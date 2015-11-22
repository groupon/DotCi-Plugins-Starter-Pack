DotCi-Plugins
=============

Extends [DotCi](https://github.com/groupon/DotCi) in the following ways

- Enables build timeout of 60 mins and Color ANSI Console Output with xterm when a new DotCi project is created.

- Adds Hipchat notification to `.ci.yml` ( Configure token under jenkins configuration)

  ```yaml
     notifications:
       - hipchat: <room name>
    ```
- Adds webhook notification support to `.ci.yml`

  ```yaml
  notifications:
   - webhook:
      url: <webhook-url>
      payload: #params POSTed to webhook as json payload
         foo:  bar
         version: $DOTCI_SHA
    ```
- Add the following plugins

 ```yaml
   plugins:
  - junit #defaults to '**/surefire-reports/*.xml', can configure
  - tap
  - checkstyle #expects file to be target/checkstyle-result.xml
  - cobertura #expects target/site/cobertura/coverage.xml
  - findbugs #expects target/findbugsXml.xml
  - jacoco  # expects **/jacoco.exec
  - pmd #expects **/pmd.xml
  ```

Please take a look at [pom.xml](/pom.xml) for full list of plugin dependencies.
##Installation
Install the plugin from Jenkins Update Center under `Manage Jenkins > Manage Plugins`

##Development Setup
 Please see [DotCi Development Setup](https://github.com/groupon/DotCi/blob/master/docs/DevelopmentSetup.md)

##LICENSE
```
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
