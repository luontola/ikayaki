xcopy /y /s /exclude:excluded.txt resources build\tmp\resources\
xcopy /y /s /exclude:excluded.txt manual build\manual\
xcopy /y /s /exclude:excluded.txt classes\ikayaki build\tmp\ikayaki\
xcopy /y /s /exclude:excluded.txt classes\jutil build\tmp\jutil\
xcopy /y src\IKAYAKI.MF build\tmp\META-INF\MANIFEST.MF
mkdir build\lib
mkdir build\jre\bin
mkdir build\jre\lib
copy /y lib\*.jar build\lib\
copy /y lib\*.dll build\jre\bin\
copy /y lib\*.properties build\jre\lib\
pause