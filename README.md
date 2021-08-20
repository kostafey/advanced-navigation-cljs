# Visual Studio Code advanced-navigation

VSCode extension in ClojureScript aimed to improve cursor navigation.

Minimal CLJS VSCode extension using shadow-cljs: [cljs-vscode-extension-hello-world](https://github.com/Saikyun/cljs-vscode-extension-hello-world)

## Features

* `exchange-point-and-mark` - Put the selection start where cursor is now, and cursor where the selection start is now.

## Quick hint

1. `npm install -g shadow-cljs`
2. `npm install`
3.  ```bash
    $ shadow-cljs clj-repl
    => (shadow/watch :dev)
    => (shadow/repl :dev)
    ```
4. Run `Command Palette`: `Calva: Connect to a Running REPL Server in the project`
* Select `shadow-cljs`
* Use `host` and `port` from `shadow-cljs clj-repl` command
* Select `:dev`.