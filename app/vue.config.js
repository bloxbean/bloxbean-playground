/**
 * @type {import('@vue/cli-service').ProjectOptions}
 */
module.exports = {
  publicPath: process.env.DEPLOY_ENV === 'GH_PAGES'
    ? '/admin/'
    : '/',

  // Remove moment.js from chart.js
  configureWebpack: config => {
    return {
      externals: {
        moment: 'moment'
      },
      experiments: {
        asyncWebAssembly: true
      }
    }
  }
}
