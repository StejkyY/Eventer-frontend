config.resolve.modules.push("../../processedResources/js/main");
config.resolve.conditionNames = ['import', 'require', 'default'];

if (config.devServer) {
    config.devServer.hot = true;
    config.devServer.compress = false; // workaround for SSE
    config.devServer.historyApiFallback = true
    config.devtool = 'eval-cheap-source-map';
} else {
    config.devtool = undefined;
}

// disable bundle size warning
config.performance = {
    assetFilter: function (assetFilename) {
      return !assetFilename.endsWith('.js');
    },
};

var webpack = require("webpack");
var path = require('path');
var dotenv = require('dotenv').config({ path: path.resolve(__dirname, '../../../../production.env') });

var definePlugin = new webpack.DefinePlugin(
    {
        "PROCESS_ENV": JSON.stringify(dotenv.parsed)
    }
);

config.plugins.push(definePlugin);
