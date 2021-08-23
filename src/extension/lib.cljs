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

(defn remove-selection []
  (let [sel (selection)]
    (set-selection (. sel -active) (. sel -active))))

(defn execute-command
  "Executes the command denoted by the given command identifier.
   `command` Identifier of the command to execute.
   `rest` Parameters passed to the command function.

   return A thenable that resolves to the returned value of the given command. 
   `undefined` when the command handler function doesn't return anything.
   "
  [command & rest]
  ((.. vscode.commands -executeCommand) command rest))

(defn cursor
  "The position of the cursor."
  []
  (. (selection) -active))

(defn following-char
  "Return the character following point, as a string."
  []
  (let [cursorPos (cursor)
        charNumber (. cursorPos -character)
        document (. (editor) -document)
        lineText (. ((. document -lineAt) cursorPos) -text)]
    (subs lineText 
          charNumber
          (if (> (+ charNumber 1) (count lineText))
            charNumber
            (+ charNumber 1)))))

(defn preceding-char
  "Return the character preceding point, as a string."
  []
  (let [cursorPos (. (selection) -active)
        charNumber (. cursorPos -character)
        document (. (editor) -document)
        lineText (. ((. document -lineAt) cursorPos) -text)]
    (subs lineText
          (if (> charNumber 0)
            (- charNumber 1)
            charNumber)
          charNumber)))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))
