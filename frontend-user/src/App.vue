<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: var(--font-family);
  color: var(--text-primary);
  background-color: var(--bg-secondary);
}

#app {
  height: 100%;
}

.el-header {
  background-color: var(--bg-navbar);
  box-shadow: var(--box-shadow-light);
  padding: 0 20px;
}

.el-main {
  padding: 20px;
  background-color: var(--bg-secondary);
}
</style> 