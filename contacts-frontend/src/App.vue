<script setup lang="ts">

import { RouterView } from 'vue-router'
import {defaultConfig} from "@/config/settings.ts";

defaultConfig.forEach((value: string, key: string) => {
  if (localStorage.getItem(key) === null) {
    localStorage.setItem(key, value)
  }
})

</script>

<template>
  <header>
<!--    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="125" height="125" />-->

<!--    <div>-->
<!--      <RouterLink to="/">Home</RouterLink>-->
<!--    </div>-->
<!--    <RouterLink to="/about">About</RouterLink>-->
  </header>

  <RouterView />
</template>

<style scoped>

</style>
