package com.blog.service.impl;

import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.SliderCheckDTO;
import com.blog.dto.SliderResultDTO;
import com.blog.entity.FailedLoginAttempts;
import com.blog.entity.Users;
import com.blog.mapper.UsersMapper;
import com.blog.result.Result;
import com.blog.service.IFailedLoginAttemptsService;
import com.blog.service.IUsersService;
import static com.blog.utils.RedisConstants.LOGIN_FAIL_KEY_PREFIX;
import static com.blog.utils.RedisConstants.CAPTCHA_PREFIX;
import static com.blog.utils.RedisConstants.SLIDER_X_PREFIX;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.utils.CaptchaUtil;
import com.blog.utils.ImageUtil;
import com.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
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
    private UsersMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<?> register(RegisterDTO registerDTO) {
        // 双重校验
        // 获取用户名
        String username = registerDTO.getUsername();
        //获取密码
        String rawPassword = registerDTO.getPassword();
        // 获取验证码
        String captcha = registerDTO.getCaptcha();

        //校验验证码
        String redisKey = CAPTCHA_PREFIX + username;
        String code = stringRedisTemplate.opsForValue().get(redisKey);
        if (code == null || !code.equals(captcha)) {
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
        }else {
            //不存在，创建新用户并保存
            user = saveUser(username, encodedPwd);
        }

        // 返回成功
        return Result.success("注册成功");
    }

    @Override
    public Result<?> login(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String failKey = LOGIN_FAIL_KEY_PREFIX + username;

        String cacheKey = stringRedisTemplate.opsForValue().get(failKey);

        //登陆失败次数统计
        int failCount = Optional.ofNullable(cacheKey)
                .map(Integer::parseInt)
                .orElse(0);


        Users user = userMapper.selectByUsername(username);
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
            if(failCount==2){//记录超过3次失败登录的用户
                FailedLoginAttempts failedLoginAttempts = new FailedLoginAttempts();
                failedLoginAttempts.setUserId(user.getId());
                failedLoginAttemptsService.save(failedLoginAttempts);
            }
            return Result.error("用户名或密码错误");
        }

        if (failCount >= 3) {
            String sliderKey = SLIDER_X_PREFIX + loginDTO.getSliderToken();
            String sliderX = stringRedisTemplate.opsForValue().get(sliderKey);
            if (sliderX == null || Math.abs(loginDTO.getSliderX() - Integer.parseInt(sliderX)) > 5) {
                return Result.error("滑块验证失败");
            }
            stringRedisTemplate.delete(sliderKey);
        }

        stringRedisTemplate.delete(failKey);
        String token = JwtUtil.generateToken(user);
        return Result.success(Collections.singletonMap("token", token));
    }

    // 生成验证码
    @Override
    public Result<?> captcha(String username) {

        if (username == null || username.length() < 6 || username.length() > 20) {
            return Result.error("用户名长度需在6-20之间");
        }
        //校验用户名是否存在
        Users user = query().eq("username", username).one();
        if(user != null){
            return Result.error("用户名已占用");
        }

        String code = CaptchaUtil.generateCode();
        String image;
        try {
            image = CaptchaUtil.createBase64Image(code);
        } catch (IOException e) {
            // 打印日志或自定义异常处理
            e.printStackTrace();
            throw new RuntimeException("验证码图片生成失败");
        }

        // 存储验证码到Redis，有效期设置为5分钟
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + username, code, 5, TimeUnit.MINUTES);

        return Result.success(Collections.singletonMap("img", image));
    }

    @Override
    public Result<?> generateSliderImage()  {
        try{
            // 加载模板图
            Resource templateResource = new ClassPathResource("templates/white.jpg");
            BufferedImage templateImage = ImageIO.read(templateResource.getInputStream());

            // 加载背景图
            Resource bgResource = new ClassPathResource("templates/background.jpg");
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
        }catch (IOException e) {
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


    // 创建用户并保存
    private Users saveUser(String username, String password){
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(password);
        save(user);
        return user;
    }
}
