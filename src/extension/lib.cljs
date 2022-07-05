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
   input most recently.
   
   ^TextDocument"
  []
  (. vscode.window -activeTextEditor))

(defn tab-groups
  "All the groups within the group container.

   ^TabGroup[]"
  []
  (. (. vscode.window -tabGroups) -all))

(defn get-active-tab [^TabGroup tab-group]
  (. tab-group -activeTab))

(def view-column
  "Denotes a location of an editor in the window. Editors can be arranged in 
   a grid and each column represents one editor location in that grid by 
   counting the editors in order of their appearance."
  {;; A *symbolic* editor column representing the currently active column. This value
   ;; can be used when opening editors, but the *resolved* {@link TextEditor.viewColumn viewColumn} -value
   ;; of editors will always be `One ``Two ``Three `... or `undefined `but never `Active `.
   :active -1,
   ;; A *symbolic* editor column representing the column to the side of the active one. This value
   ;; can be used when opening editors, but the *resolved* {@link TextEditor.viewColumn viewColumn}-value
   ;; of editors will always be `One`, `Two`, `Three`,... or `undefined` but never `Beside`. 
   :beside -2,
   :one 1, ; The first editor column. 
   :two 2, ;; The second editor column.
   :three 3, ;; The third editor column.
   :four 4, ;; The fourth editor column.
   :five 5, ;; The fifth editor column.
   :six 6, ;; The sixth editor column.
   :seven 7, ;; The seventh editor column. 
   :eight 8, ;; The eighth editor column. 
   :nine 9 ;; The ninth editor column.
   })

(defn show-text-document-by-uri
  "Show the given document in a text editor.
   Return a promise that resolves to an TextEditor editor.
   
   ^Thenable<TextEditor>"
  [^Uri uri
   ^Map<TextDocumentShowOptions> options]
  (.. vscode.window (showTextDocument
                     uri
                     (clj->js options))))

(defn show-text-document
  "Show the given document in a text editor.
   Return a promise that resolves to an TextEditor editor.
   
   ^Thenable<TextEditor>"
  ([^TextDocument document]
   (show-text-document-by-uri (. document -uri) {}))
  ([^TextDocument document
    ^Map<TextDocumentShowOptions> options]
   (show-text-document-by-uri (. document -uri) (clj->js options))))

(defn close-tab
  "Closes the tab. This makes the tab object invalid and the tab
   should no longer be used for further actions.
   
   ^Thenable<boolean>"
  [^Tab tab]
  (. (. vscode.window -tabGroups) (close tab)))

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
