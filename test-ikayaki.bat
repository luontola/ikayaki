:: "test-driver" for Ikayaki (and MainViewPanel, and the rest...)

@echo off
setlocal

set CLASSPATH=classes;lib\comm.jar;lib\forms_rt.jar;lib\junit.jar;lib\looks.jar;lib\vecmath.jar

:: doesn't work
:: for %%f in (classes lib\*.jar) do set CLASSPATH=%CLASSPATH%;%%f

echo -- 1/6 starting without parameters...
java ikayaki.Ikayaki
echo.

set ikayaki=*/?
echo -- 2/6 starting with parameter "%ikayaki%"...
java ikayaki.Ikayaki %ikayaki%
echo.

set ikayaki=projects\test.ika
echo -- 3/6 starting with parameter "%ikayaki%"...
java ikayaki.Ikayaki %ikayaki%
echo.

ren ikayaki.config ikayaki.config.test
echo -- 4/6 starting without parameters AND without config file...
java ikayaki.Ikayaki
echo.

echo -- 5/6 starting without parameters, with config file left from previous...
java ikayaki.Ikayaki
echo.

del ikayaki.config
echo -- 6/6 starting with parameter "%ikayaki%" AND without config file...
java ikayaki.Ikayaki %ikayaki%
echo.

del ikayaki.config
ren ikayaki.config.test ikayaki.config

echo -- if everything worked fine, good :)

endlocal
