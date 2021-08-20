(ns extension.lib)

(def vscode (js/require "vscode"))

(defn register-command
  "Registers a command that can be invoked via a keyboard shortcut,
   a menu item, an action, or directly.

   Registering a command with an existing command identifier twice
   will cause an error.

   `command` A unique identifier for the command.
   `callback` A command handler function.
   return Disposable which unregisters this command on disposal."
  [command callback]
  (.. vscode.commands
      (registerCommand command callback)))

(defn message
  "Show an information message to users."
  [msg]
  (.. vscode.window (showInformationMessage msg)))

(defn editor
  "The currently active editor or `undefined`. The active editor is the one
   that currently has focus or, when none has focus, the one that has changed
   input most recently."
  []
  (. vscode.window -activeTextEditor))

(defn selection
  "The primary selection on this text editor.
   Shorthand for `TextEditor.selections[0]`."
  []
  (. (editor) -selection))

(defn new-selection
  "Create a selection from two positions."
  [start end]
  (vscode.Selection. start end))

(defn set-selection [start end]
  (set! (. (editor) -selection) (new-selection start end)))