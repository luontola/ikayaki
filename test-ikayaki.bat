:: "test-driver" for Ikayaki (and MainViewPanel, and the rest...)

@echo off
setlocal

set CLASSPATH=classes;lib\comm.jar;lib\forms_rt.jar;lib\junit.jar;lib\looks.jar;lib\vecmath.jar

:: doesn't work
:: for %%f in (classes lib\*.jar) do set CLASSPATH=%CLASSPATH%;%%f

echo -- starting without parameters...
java ikayaki.Ikayaki
echo.

set ikayaki=*/?
echo -- starting with parameter "%ikayaki%"...
java ikayaki.Ikayaki %ikayaki%
echo.

set ikayaki=projects\test.ika
echo -- starting with parameter "%ikayaki%"...
java ikayaki.Ikayaki %ikayaki%
echo.

ren ikayaki.config ikayaki.config.test
echo -- starting without parameters AND without config file...
java ikayaki.Ikayaki
echo.

del ikayaki.config
echo -- starting with parameter "%ikayaki%" AND without config file...
java ikayaki.Ikayaki %ikayaki%
echo.

del ikayaki.config
ren ikayaki.config.test ikayaki.config

echo -- if everything worked fine, good :)

endlocal
