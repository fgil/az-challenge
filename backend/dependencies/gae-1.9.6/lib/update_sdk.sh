#/bin/bash
rm -rf *.jar
find $GAE_PATH/lib -name \*.jar -exec cp -f {} . \;
