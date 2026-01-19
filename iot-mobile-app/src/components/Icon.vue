<template>
  <view class="icon-wrapper" :style="{ width: size + 'px', height: size + 'px' }" v-html="iconSvg"></view>
</template>

<script>
import { renderIcon } from '@/utils/icons'

export default {
  name: 'Icon',
  props: {
    name: {
      type: String,
      required: true
    },
    size: {
      type: Number,
      default: 24
    },
    color: {
      type: String,
      default: 'currentColor'
    }
  },
  computed: {
    iconSvg() {
      return renderIcon(this.name, this.size, this.color)
    }
  }
}
</script>

<style scoped>
.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper :deep(svg) {
  width: 100%;
  height: 100%;
}
</style>
