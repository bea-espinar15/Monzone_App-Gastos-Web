function fn() {
    var env = karate.env; // get system property 'karate.env'
    karate.log('karate.env system property was:', env);

    if (!env) {
        env = 'dev';
    }

    /**
     * Variables here are available in all tests
     */
    var config = {
        env: env,
        myVarName: 'someValue',
        baseUrl: 'http://localhost:8080'
    }

    /**
     * Drivers for tests - currently configured value is good for Linux
     */
    karate.configure('driver', {
        type: 'chrome',
        // descomentar para chromium bajo linux
        // executable: '/usr/bin/chromium-browser',

        // "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"
        executable:"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe" , // Poner la ruta donde este el .exe de Chrome en tu ordenador
        
        addOptions: ["--remote-allow-origins=*", "--incognito"],
        showDriverLog: true
    })

    if (env == 'dev') {
        // customize
        // e.g. config.foo = 'bar';
    } else if (env == 'e2e') {
        // customize
    }
    return config;
}