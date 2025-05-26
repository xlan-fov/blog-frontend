package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.blog.dto.*;
import com.blog.entity.FailedLoginAttempts;
import com.blog.entity.Users;
import com.blog.mapper.AdminSecretMapper;
import com.blog.mapper.UsersMapper;
import com.blog.result.Result;
import com.blog.service.IFailedLoginAttemptsService;
import com.blog.service.IUsersService;
import static com.blog.utils.RedisConstants.LOGIN_FAIL_KEY_PREFIX;
import static com.blog.utils.RedisConstants.CAPTCHA_PREFIX;
import static com.blog.utils.RedisConstants.SLIDER_X_PREFIX;
import static com.blog.utils.RedisConstants.LOGIN_USER_KEY;
//import static com.blog.utils.RedisConstants.REGISTER_CODE_KEY;
import static com.blog.utils.RedisConstants.CODE_KEY;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.utils.*;
import com.blog.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Slf4j
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    /*
     * @Author: guoyuran
     * @Date: 2025-5-6
     * @Description: 用户注册登录
     */

    @Autowired
    private IFailedLoginAttemptsService failedLoginAttemptsService;

    @Autowired
    private IUsersService usersService;

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private AdminSecretMapper adminSecretMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result<?> registerByname(RegisterDTO registerDTO) {
        try {
            log.info("开始处理注册请求: username={}, captchaId={}", registerDTO.getUsername(), registerDTO.getCaptchaId());
            
            // 双重校验
            // 获取用户名
            String username = registerDTO.getUsername();
            if (username == null || username.isEmpty()) {
                return Result.error("用户名不能为空");
            }
            
            //获取密码
            String rawPassword = registerDTO.getPassword();
            if (rawPassword == null || rawPassword.isEmpty()) {
                return Result.error("密码不能为空");
            }
            
            // 获取验证码和ID
            String captcha = registerDTO.getCaptcha();
            String captchaId = registerDTO.getCaptchaId();
            if (captcha == null || captcha.isEmpty() || captchaId == null || captchaId.isEmpty()) {
                return Result.error("验证码信息不完整");
            }

            //校验验证码
            String redisKey = CAPTCHA_PREFIX + captchaId;
            String code = stringRedisTemplate.opsForValue().get(redisKey);
            log.debug("从Redis获取的验证码: {}, 提交的验证码: {}", code, captcha);
            if (code == null || !code.equalsIgnoreCase(captcha)) {
                log.info("验证码校验失败: Redis验证码为 {}, 提交验证码为 {}", code, captcha);
                return Result.error("验证码错误或已过期");
            }

            if (username.length() < 6 || username.length() > 20) {
                return Result.error("用户名长度需在6-20之间");
            }
            
            String passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,16}$";
            if (!rawPassword.matches(passwordPattern)) {
                return Result.error("密码不符合规范");
            }

            // 密码加密
            String encodedPwd = passwordEncoder.encode(rawPassword);

            //校验用户名是否存在
            Users user = query().eq("username", username).one();
            if(user != null){
                return Result.error("用户名已占用");
            }
            
            //不存在，创建新用户并保存
            user = new Users();
            user.setUsername(username);
            user.setPassword(encodedPwd);
            user.setCreatedAt(new Date());
            user.setIsBanned(0);
            user.setIsLoggedIn(0);
            user.setIsDeleted(0);
            user.setRole("user"); // 设置为普通用户角色
            
            log.info("准备保存新用户: {}", user);
            boolean result = save(user);
            log.info("用户保存结果: {}, 用户ID: {}", result, user.getId());
            
            if (!result) {
                return Result.error("注册失败，请稍后重试");
            }

            // 注册成功后删除验证码
            stringRedisTemplate.delete(redisKey);
            
            // 返回成功
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册过程发生异常: ", e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> loginByname(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String failKey = LOGIN_FAIL_KEY_PREFIX + username;

        // 获取验证码
        String captcha = loginDTO.getCaptcha();
        // 获取验证码ID
        String captchaId = loginDTO.getCaptchaId();

        //校验验证码
        String redisKey = CAPTCHA_PREFIX + captchaId;
        String code = stringRedisTemplate.opsForValue().get(redisKey);
        if (code == null || !code.equalsIgnoreCase(captcha)) {
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
            return Result.error("验证码错误或已过期");
        }

        String cacheKey = stringRedisTemplate.opsForValue().get(failKey);

        //登陆失败次数统计
        int failCount = Optional.ofNullable(cacheKey)
                .map(Integer::parseInt)
                .orElse(0);

        Users user = userMapper.selectByUsernameNotbanned(username);
        Users user2 = userMapper.selectByUsername(username);
        if(user == null && user2 == null){
            return Result.error("用户名未注册");
        } else if(user == null) {
            return Result.error("用户被封禁");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
            if(failCount==2){
                FailedLoginAttempts failedLoginAttempts = new FailedLoginAttempts();
                failedLoginAttempts.setUserId(user.getId());
                failedLoginAttemptsService.save(failedLoginAttempts);
            }
            return Result.error("密码错误");
        }



        if (failCount >= 3) {
            return Result.error("请先进行滑动验证！");
        }

        // 生成新的token，确保每次登录都是唯一的
        String token = JwtUtil.generateToken(user);
        log.info("为用户 {} 生成新token: {}", username, token.substring(0, 20) + "...");

        // 更新用户登录状态
        user.setLastLoginTime(new Date());
        user.setIsLoggedIn(1);
        userMapper.updateById(user); // 更新用户信息

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPhone(user.getPhone());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setBio(user.getBio());
        userDTO.setRole(user.getRole());
        userDTO.setIsBanned(user.getIsBanned());
        userDTO.setIsLoggedIn(user.getIsLoggedIn());
        userDTO.setLastLoginTime(user.getLastLoginTime());
        userDTO.setCreatedAt(user.getCreatedAt());

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue != null ? fieldValue.toString() : null));

        // 将新token存入Redis
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, 1800, TimeUnit.SECONDS);

        // 删除验证码（防止重复使用）
        stringRedisTemplate.delete(redisKey);
        
        // 清除登录失败计数
        stringRedisTemplate.delete(failKey);

        // 返回包含用户信息的数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("id", user.getId()); // 确保返回用户ID
        responseData.put("userId", user.getId()); // 兼容不同的字段名
        responseData.put("username", user.getUsername());
        responseData.put("role", user.getRole());
        responseData.put("status", user.getIsBanned() == 1 ? "banned" : "normal");

        return Result.success(responseData);
    }

    @Override
    public Result<?> adminLogin(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String failKey = LOGIN_FAIL_KEY_PREFIX + username;
        String secretCode = loginDTO.getSecretCode();

        Users user = userMapper.selectByUsername(username);
        
        if(user == null){
            return Result.error("管理员未注册");
        }
        Integer userId = user.getId();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
            return Result.error("密码错误");
        }

        // Get the admin secret code safely
        String adminSecret = adminSecretMapper.selectByUserId(userId);
        
        // Null-safe comparison
        if(adminSecret == null || !adminSecret.equals(secretCode)){
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
            return Result.error("权限码错误");
        }

        String value = stringRedisTemplate.opsForValue().get(failKey);
        //登陆失败次数统计
        int failCount = Optional.ofNullable(value)
                .map(Integer::parseInt)
                .orElse(0);
        if (failCount >= 3) {
            //stringRedisTemplate.delete(failKey);
            return Result.error("请先进行滑动验证！");
        }

        /* 
        LambdaUpdateWrapper<Users> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Users::getId, user.getId())
                .set(Users::getLastLoginTime, new Date());

        usersService.update(updateWrapper);
        */
        //System.out.println("\n\n用户id：" + user.getId()+"\n\n");
        user.setLastLoginTime(new Date());
        user.setIsLoggedIn(1); // 设置为已登录状态
        userMapper.updateById(user); // 更新用户信息
        //System.out.println("\n\n用户登录状态 ：" + user.getIsLoggedIn()+"\n\n");
        String token = JwtUtil.generateToken(user);

        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue != null ? fieldValue.toString() : null));

        // 将 token 存入 Redis，设置过期时间（单位：秒）
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, 1800, TimeUnit.SECONDS);

        //userMapper.update1ByUsername(username);
        //UserDTO user1 = UserHolder.getUser();
        //String username1 = user1.getUsername();

        return Result.success(Collections.singletonMap("token", token));

    }

    //@Override
    //public Result<?> registerByphone(PhoneDTO phoneDTO) {
    //    String phone = phoneDTO.getPhone();
    //    if(PatternsUtil.isPhoneInvalid(phone)){
    //        // 如果不符合，返回错误信息
    //        return Result.error("手机号格式错误！");
    //    }
    //
    //    String codeKey = stringRedisTemplate.opsForValue().get(CODE_KEY + phone);
    //    String code = phoneDTO.getCode();
    //    // 不一致，报错
    //    if(codeKey == null || !codeKey.equals(code)){
    //        return Result.error("验证码失效或错误！");
    //    }
    //    // 一致，根据手机号查询用户
    //    //MyBatis-Plus的条件查询，ServiceImpl<UserMapper, User>里定义了query，可以直接用
    //    Users user = query().eq("phone", phone).one();
    //
    //    // 判断用户是否存在
    //    if(user == null){
    //        // 不存在，创建新用户并保存
    //        user = saveUser(phone);
    //
    //    }else{
    //        return Result.error("手机号已注册");
    //    }
    //
    //    return Result.success("注册成功");
    //}

    @Override
    public Result<?> loginByphone(PhoneDTO phoneDTO, HttpServletRequest request) {
        String phone = phoneDTO.getPhone();
        if(PatternsUtil.isPhoneInvalid(phone)){
            // 如果不符合，返回错误信息
            return Result.error("手机号格式错误！");
        }

        String codeKey = stringRedisTemplate.opsForValue().get(CODE_KEY + phone);
        String code = phoneDTO.getCode();

        // 不一致，报错
        if(codeKey == null || !codeKey.equals(code)){
            return Result.error("验证码失效或错误！");
        }

        //if (failCount >= 3) {
            //String sliderKey = SLIDER_X_PREFIX + phoneDTO.getSliderToken();
            //String sliderX = stringRedisTemplate.opsForValue().get(sliderKey);
            //if (sliderX == null || Math.abs(phoneDTO.getSliderX() - Integer.parseInt(sliderX)) > 5) {
            //stringRedisTemplate.delete(failKey);
            //return Result.error("滑块验证失败");
            //}
        //}

        // 一致，根据手机号查询用户
        //MyBatis-Plus的条件查询，ServiceImpl<UserMapper, User>里定义了query，可以直接用
        Users user = query().eq("phone", phone).one();

        // 判断用户是否存在
        if(user == null){
            log.debug("手机号未注册");
            // 不存在，创建新用户并保存
            user = saveUser(phone);
        }else{
            log.debug("手机号已注册");
        }

        String token = JwtUtil.generateToken(user);

        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue != null ? fieldValue.toString() : null));

        // 将 token 存入 Redis，设置过期时间（单位：秒）
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, 1800, TimeUnit.SECONDS);
        userMapper.update1ByPhone(phone);

        return Result.success(Collections.singletonMap("token", token));
    }

    // 使用phone登陆注册时调用
    @Override
    public Result<?> sendCode(String phone) {
        // 校验手机号
        if(PatternsUtil.isPhoneInvalid(phone)){
            // 如果不符合，返回错误信息
            return Result.error("手机号格式错误！");
        }

        // 符合，生成验证码
        // 由于短信服务商限制，验证码只能是6位数字
        String code = RandomUtil.randomNumbers(6);
        boolean result = SMSUtil.sendSms(phone, code);
        if(result){
            // 发送验证码
            log.debug("发送短信验证码成功，验证码：{}",code);
            // 保存验证码到redis  也就是 set key value ex 120
            stringRedisTemplate.opsForValue().set(CODE_KEY + phone, code, 2, TimeUnit.MINUTES);
        }
        // 返回ok
        return Result.success();
    }

    // 生成验证码--username
    @Override
    public Result<?> captcha(String captchaId) {
        if(captchaId == null){
            return Result.error("验证码ID不能为空");
        }

        String code = CaptchaUtil.generateCode();
        log.debug("获取验证码为："+code);
        String image;
        try {
            image = CaptchaUtil.createBase64Image(code);
        } catch (IOException e) {
            // 打印日志或自定义异常处理
            e.printStackTrace();
            throw new RuntimeException("验证码图片生成失败");
        }

        // 存储验证码到Redis，有效期设置为5分钟
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, code.toUpperCase(), 5, TimeUnit.MINUTES);

        return Result.success(Collections.singletonMap("img", image));
    }

    @Override
    public Result<?> generateSliderImage() {
        try {
            // 加载模板图
            Resource templateResource = new ClassPathResource("templates/white.jpg");
            BufferedImage templateImage = ImageIO.read(templateResource.getInputStream());

            // 加载背景图
            Resource bgResource = new ClassPathResource("templates/newBackground.jpg");
            BufferedImage bgImage = ImageIO.read(bgResource.getInputStream());

            // 裁剪抠图 & 加阴影
            BufferedImage[] results = ImageUtil.cutByTemplate(bgImage, templateImage);
            BufferedImage bgWithShadow = results[0];
            BufferedImage block = results[1];

            int y = ImageUtil.getLastY();
            int x = ImageUtil.getLastX();

            String token = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set(SLIDER_X_PREFIX + token, String.valueOf(x), 2, TimeUnit.MINUTES);

            SliderResultDTO sliderResultDTO = new SliderResultDTO();
            sliderResultDTO.setBackgroundImage(ImageUtil.toBase64(bgWithShadow));
            sliderResultDTO.setSliderImage(ImageUtil.toBase64(block));
            sliderResultDTO.setY(y);
            sliderResultDTO.setToken(token);

            return Result.success(sliderResultDTO);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("滑块生成失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> validateSlider(SliderCheckDTO sliderCheckDTO) {
        String redisX = stringRedisTemplate.opsForValue().get(SLIDER_X_PREFIX + sliderCheckDTO.getToken());
        if (redisX != null && Math.abs(Integer.parseInt(redisX) - sliderCheckDTO.getUserX()) <= 5) {
            stringRedisTemplate.delete(SLIDER_X_PREFIX + sliderCheckDTO.getToken());
            return Result.success("验证通过");
        }
        return Result.error("滑块验证失败");
    }

    @Override
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.error("请先登录");
        }

        UserDTO user = UserHolder.getUser();
        if (user != null) {
            String phone = user.getPhone();
            String username = user.getUsername();
            if (username != null) {
                userMapper.update0ByUsername(username);
            } else if (phone != null) {
                userMapper.update0ByPhone(phone);
            }
            log.info("用户 {} 退出登录", username != null ? username : phone);
        }

        // 删除Redis中保存的token
        String redisKey = LOGIN_USER_KEY + token;
        Boolean deleted = stringRedisTemplate.delete(redisKey);
        log.info("删除token结果: {}, token: {}", deleted, token.substring(0, 20) + "...");

        return Result.success("退出成功");
    }

    // 根据用户名创建用户并保存
    private Users saveUser(String username, String password) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreatedAt(new Date()); // 添加创建时间
        
        log.info("准备保存用户: {}", username);
        try {
            boolean result = save(user);
            log.info("用户保存结果: {}, 用户ID: {}", result, user.getId());
            return user;
        } catch (Exception e) {
            log.error("保存用户失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 根据手机号创建用户并保存
    private Users saveUser(String phone) {
        Users user = new Users();
        user.setPhone(phone);
        user.setCreatedAt(new Date()); // 添加创建时间
        
        log.info("准备保存手机用户: {}", phone);
        try {
            boolean result = save(user);
            log.info("手机用户保存结果: {}, 用户ID: {}", result, user.getId());
            return user;
        } catch (Exception e) {
            log.error("保存手机用户失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Result<?> getProfile(){
        Users user = userMapper.selectById(UserHolder.getUser().getId());
        if(user == null){
            return Result.error("用户不存在");
        }
        // 获取用户信息
        // 用户ID，用户名，手机号，头像，描述（text），（），是否登录，最后登录时间，创建时间
        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setId(user.getId());
        userProfileVO.setUsername(user.getUsername());
        userProfileVO.setPhone(user.getPhone());
        userProfileVO.setAvatar(user.getAvatarUrl());
        userProfileVO.setDescription(user.getBio());
        userProfileVO.setLastLoginTime(user.getLastLoginTime());
        userProfileVO.setRegisterTime(user.getCreatedAt());
        return Result.success(userProfileVO);
    }

    @Override
    public Result<?> updateProfile(UserProfileDTO userProfileDTO){
        // 判断当前登录用户是否是要修改的用户
        if (!Objects.equals(UserHolder.getUser().getId(), userProfileDTO.getId())) {
            return Result.error("不能修改其他用户信息");
        }
        // 获取当前登录用户
        Users currentUser = userMapper.selectById(UserHolder.getUser().getId());
        if(currentUser == null){
            // 没啥用，一般可以登录那肯定有
            return Result.error("用户不存在");
        }
        // 判断用户名是否已存在
        Users existingUser = userMapper.selectByUsername(userProfileDTO.getUsername());
        if (existingUser != null && !existingUser.getId().equals(userProfileDTO.getId())) {
            return Result.error("用户名已存在");
        }

        // 更新用户信息
        Users updatedUser = new Users();
        updatedUser.setId(userProfileDTO.getId());
        updatedUser.setUsername(userProfileDTO.getUsername());
        updatedUser.setBio(userProfileDTO.getDescription());

        // 更新数据库
        boolean result = userMapper.updateById(updatedUser) > 0;
        if(result){
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
    @Override
    public Result<?> updatePassword(UserPasswdChangeDTO userPasswdChangeDTO){
        // 判断当前登录用户是否是要修改的用户
        if (!Objects.equals(UserHolder.getUser().getId(), userPasswdChangeDTO.getId())) {
            return Result.error("不能修改其他用户密码");
        }
        // 获取当前登录用户
        String currentPasswd = userMapper.getPasswordById(userPasswdChangeDTO.getId());
        // 判断旧密码是否正确
//        System.out.println("当前用户密码：" + currentUser.getPassword());
//        System.out.println("输出的旧密码：" + userPasswdChangeDTO.getOldPassword());
        if (!passwordEncoder.matches(userPasswdChangeDTO.getOldPassword(), currentPasswd)) {
            return Result.error("旧密码错误");
        }
        // 判断新密码是否符合规范
        String newPassword = userPasswdChangeDTO.getNewPassword();
        String passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,16}$";
        if (!newPassword.matches(passwordPattern)) {
            return Result.error("新密码不符合规范");
        }
        // 密码加密
        String encodedPwd = passwordEncoder.encode(newPassword);
        // 更新用户密码
        // 更新数据库
        userMapper.updateUserPassword(userPasswdChangeDTO.getId(), encodedPwd);
        // 更新成功
        return Result.success("密码修改成功");
    }
}
