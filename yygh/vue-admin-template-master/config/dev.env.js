'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  //BASE_API: '"http://localhost:9001"',//nginx的路径
  BASE_API: '"http://localhost:80"',//Springcloudgateway网关路径
})
