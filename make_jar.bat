xcopy /y /s /exclude:excluded.txt resources build\resources\
xcopy /y /s /exclude:excluded.txt classes\ikayaki build\ikayaki\
xcopy /y /s /exclude:excluded.txt classes\jutil build\jutil\
xcopy /y src\IKAYAKI.MF build\META-INF\MANIFEST.MF
mkdir build\lib
copy /y lib\*.jar build\lib\
copy /y lib\*.dll build\lib\
copy /y lib\*.properties build\lib\
pause