const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
    // in prod look at shadow-cljs output file in dev look at runtime, which will change files that are actually compiled; postcss watch should be a whole lot faster
    // TODO: fix prod
    // content: process.env.NODE_ENV == 'production' ? ["./target/classes/cljsbuild/public/js/app.js"] : ["./target/classes/cljsbuild/public/js/cljs-runtime/*.js"],
    content: process.env.NODE_ENV == 'production' ? ["./target/classes/cljsbuild/public/js/app.js"] : ["./target/classes/cljsbuild/public/js/app.js"],
    theme: {
        extend: {
            fontFamily: {
                sans: ["Inter var", ...defaultTheme.fontFamily.sans],
            },
        },
    },
    plugins: [
        require('@tailwindcss/forms'),
    ],
}
