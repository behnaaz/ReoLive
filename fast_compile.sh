#!/bin/sh

# Compile all JavaScript (JS), both in localJS and in RemoteJS
sbt fastOptJS

# link JS at localJS resources (html)
cp localJS/target/scala-2.12/local_js-fastopt.js \
        localJS/src/main/resources/
cp localJS/target/scala-2.12/local_js-fastopt.js.map \
        localJS/src/main/resources/ 

# clean (if copies exist)
#rm server/app/assets/javascripts/local_js-opt.js \
#   server/app/assets/javascripts/local_js-opt.js.map \
#   server/app/assets/javascripts/remote_js-opt.js \
#   server/app/assets/javascripts/remote_js-opt.js.map

# Link JS from localJS to the server
cp localJS/target/scala-2.12/local_js-fastopt.js \
        server/app/assets/javascripts/local_js-opt.js
cp localJS/target/scala-2.12/local_js-fastopt.js.map \
        server/app/assets/javascripts/

# Link JS from remoteJS to the server
cp remoteJS/target/scala-2.12/remote_js-fastopt.js \
        server/app/assets/javascripts/remote_js-opt.js
cp remoteJS/target/scala-2.12/remote_js-fastopt.js.map \
        server/app/assets/javascripts/

# Copy stylesheet from localJS to server
cp localJS/src/main/resources/css/style.css \
   server/app/assets/stylesheets/

# Copy syntax-highlighting from localJS to server
mkdir -p server/public/javascripts/codemirror/mode/preo
cp localJS/src/main/resources/js/codemirror/mode/preo/preo.js \
   server/public/javascripts/codemirror/mode/preo/preo.js
mkdir -p server/public/javascripts/codemirror/mode/modal
cp localJS/src/main/resources/js/codemirror/mode/modal/modal.js \
   server/public/javascripts/codemirror/mode/modal/modal.js
mkdir -p server/public/javascripts/codemirror/mode/lince
cp localJS/src/main/resources/js/codemirror/mode/lince/lince.js \
   server/public/javascripts/codemirror/mode/lince/lince.js
mkdir -p server/app/assets/stylesheets/codemirror
cp localJS/src/main/resources/css/codemirror/codemirror.css \
   server/app/assets/stylesheets/codemirror/codemirror.css

# Compile the server, after having the updated JS
sbt server/compile

echo "\n - Run the server using 'sbt server/run', and access it via http://localhost:9000."