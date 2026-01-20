// Node.js 兼容性修复脚本
// 在构建前运行此脚本以修复 Node.js 新版本的兼容性问题

const fs = require('fs')
const path = require('path')

// 修复 url-loader.js
const urlLoaderPath = path.join(__dirname, 'node_modules', '.pnpm', '@dcloudio+uni-cli-shared@2.0.2-4080720251210002', 'node_modules', '@dcloudio', 'uni-cli-shared', 'lib', 'url-loader.js')

if (fs.existsSync(urlLoaderPath)) {
  let content = fs.readFileSync(urlLoaderPath, 'utf8')
  
  // 检查是否已经修复过
  if (!content.includes('util.isRegExp polyfill')) {
    const polyfill = `const util = require('util')

// 兼容 Node.js 新版本：util.isRegExp 已被移除，需要添加 polyfill
if (!util.isRegExp) {
  util.isRegExp = function(obj) {
    return Object.prototype.toString.call(obj) === '[object RegExp]'
  }
}

`
    
    // 在 fileLoader require 之后添加
    content = content.replace(
      /const fileLoader = require\('\.\/file-loader\.js'\)/,
      `const fileLoader = require('./file-loader.js')
const util = require('util')

// 兼容 Node.js 新版本：util.isRegExp 已被移除，需要添加 polyfill
if (!util.isRegExp) {
  util.isRegExp = function(obj) {
    return Object.prototype.toString.call(obj) === '[object RegExp]'
  }
}`
    )
    
    fs.writeFileSync(urlLoaderPath, content, 'utf8')
    console.log('✅ 已修复 url-loader.js')
  } else {
    console.log('ℹ️  url-loader.js 已经修复过了')
  }
} else {
  console.log('⚠️  未找到 url-loader.js 文件，可能需要先安装依赖')
}

console.log('✅ Node.js 兼容性修复完成')
