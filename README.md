# Testsuite [![Build Status](https://travis-ci.org/hal/testsuite.svg?branch=master)](https://travis-ci.org/hal/testsuite)
Selenium testsuite for the HAL management console. It uses [Drone](https://docs.jboss.org/author/display/ARQ/Drone) and [Graphene](https://docs.jboss.org/author/display/ARQGRA2/Home) Arquillian extensions.

## Prerequisites

* JDK 8 or higher
* Maven 3.0.4 or higher
* Firefox browser (tested on 31.2.0 esr version, will probably not run with far older or younger versions)
* Wildfly (10 or higher) or EAP (7 or higher)
* To setup WildFly/EAP for HAL RBAC tests please follow [RBAC.md](RBAC.md) instructions.

You can download it here:
<http://wildfly.org/downloads/> or <http://www.jboss.org/products/eap/download/>

Currently it's necessary to unsecure the management http-interface to be able to run testsuite.
E.g. for standalone Wildfly 10 using CLI:
```
/core-service=management/management-interface=http-interface:undefine-attribute(name=security-realm)
:reload
```

## How to run tests

`mvn test -P{module},{server.mode} -Djboss.dist=${path_to_server_home} -Darq.extension.webdriver.firefox_binary=${path_to_firefox_binary}
 -Djbeap2168workaround`

### Required profile (-P) parameters

Can be one of those:
* `-Pbasic,standalone` ... run basic tests against standalone mode
* `-Pbasic,domain` ... run basic tests against domain mode
* `-Prbac,standalone` ... run RBAC related tests against standalone mode
* `-Prbac,domain` ... run RBAC related tests against domain mode
* `-P[basic,rbac],[domain,standalone],serverCheck` ... run tests against given mode and at the end of every test class run
    * take snapshot of server xml configuration file
    * check if server requires reload or restart
    * in domain mode check if running states of servers don't change after test class run

### Required jboss.dist parameter

Path to server home folder. Server is expected to be already manually started.
In case of standalone mode standalone-full-ha configuration is expected.
E.g. `-Djboss.dist=/home/user/workspace/wildfly/build/target/wildfly-9.0.0.Alpha2-SNAPSHOT/`

### Optional arq.extension.webdriver.firefox_binary parameter

Path to Firefox binary file. If not provided system default firefox will be used.
E.g. `-Darq.extension.webdriver.firefox_binary=/home/user/apps/firefox-31.2.0esr/firefox`

### Optional take.screenshot.after.each.test parameter

If screenshot should be made after each test. Default is `false`.
E.g. `-Dtake.screenshot.after.each.test=true`

### Optional federation.already.enabled parameter

If PicketLink federation subsystem should be enabled for related tests
(otherwise it is assumed the subsystem is already configured and should not be added before tests and removed after them)
E.g. `-Dfederation.already.enabled=true`

### Optional propagate.to.model.timeout

Sometimes the model resource change is not yet propagated fast enough
even if in GUI the change seems to be already persisted.
This parameter means how long to wait for GUI change to be propagated to model in milis.
Default value is 500.

### Optional jbeap2168workaround

Until https://issues.jboss.org/browse/JBEAP-2168 is fixed you can use this optional parameter to switch on workaround
 to re-navigate up to 2 times again if navigation seems to be frozen.
E.g. `-Djbeap2168workaround`

## Modules

### common

Should contain all common abstraction like UI navigation, test categories, DMR abstraction etc.
It's intentioned to be able to used as a dependency of external testsuites as well.

### basic

Should contain majority of tests which don't require special configuration.

### rbac

Should contain RBAC related tests. To setup WildFly/EAP for HAL RBAC tests please follow [RBAC.md](RBAC.md) instructions.

## Tips

* If you want tests to be run on background use vncserver. E.g.

`vncserver :10 -geometry 1920x1080`

`export DISPLAY=:10`


## Known issues

* No sreenshots on test failure neither test error currently (It seems Arquillian unlike Surefire thinks they passed).
* Multiple aliases for tested IP address (usually 127.0.0.1) in hosts file has to be avoided - see [related Selenium issue](https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/3280)

## Problems?

Ping us on IRC freenode.net#wildfly-management
[Issue tracking](https://issues.jboss.org/browse/HAL/)

Have fun.
