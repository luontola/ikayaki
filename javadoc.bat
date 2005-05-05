@echo off
"D:\Program Files\Java\jdk1.5.0_02\bin\javadoc.exe" -private -author -version -link http://java.sun.com/j2se/1.5.0/docs/api/ -sourcepath src -windowtitle "Ikayaki" -d docs\api -subpackages ikayaki -subpackages hourparser -subpackages jutil -classpath lib\comm.jar;lib\forms_rt.jar;lib\junit.jar;lib\looks.jar;lib\vecmath.jar
echo.
pause