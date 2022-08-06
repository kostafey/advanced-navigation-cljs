(ns extension.lib
  (:require [clojure.string :as s]))

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
  (.. vscode.window (showInformationMessage msg))
  msg)

(defn get-configuration
  "Return a value from this configuration.

   ^T | undefined"
  [^string section]
  (.get (.. vscode.workspace getConfiguration) section))

(defn editor
  "The currently active editor or `undefined`. The active editor is the one
   that currently has focus or, when none has focus, the one that has changed
   input most recently.
   
   ^TextEditor"
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

(defn uri
  "A universal resource identifier representing either a file on disk
   or another resource, like untitled resources.

   ^Uri"
  [^string path]
  (.. vscode.Uri (file path)))

(defn show-text-document-by-uri
  "Show the given document in a text editor.
   Return a promise that resolves to an TextEditor editor.
   
   ^Thenable<TextEditor>"
  ([^Uri uri]
   (show-text-document-by-uri uri (clj->js {})))
  ([^Uri uri
    ^Map<TextDocumentShowOptions> options]
   (.. vscode.window (showTextDocument
                      uri
                      (clj->js options)))))

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
   Shorthand for `TextEditor.selections[0]`.
   
   ^Selection"
  []
  (. (editor) -selection))

(defn new-selection
  "Create a selection from two positions."
  [start end]
  (vscode.Selection. start end))

(defn set-selection [start end]
  (set! (. (editor) -selection) (new-selection start end)))

(defn goto-char [^Position position]
  (set-selection position position))

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
  "The position of the cursor.
   
   ^Position"
  []
  (. (selection) -active))

(extend-type vscode.Position
  Object
  (toString [this]
    (str "Position: " {:line (. this -line)
                       :character (. this -character)})))

(defn position
  "Represents a line and character position, such as
   the position of the cursor.
   
   ^Position"
  ([^Vector<^number> pos]
   (position (first pos) (second pos)))
  ([^number line ^number character]
   (vscode.Position. line character)))

(extend-type vscode.Range
  Object
  (toString [^Range this]
    (let [^Position start-pos (. this -start)
          ^Position end-pos (. this -end)]
      (str "Range: " {:start {:line (. start-pos  -line)
                              :character (. start-pos  -character)}
                      :end {:line (. end-pos -line)
                            :character (. end-pos -character)}}))))

(defn vs-range
  "A range represents an ordered pair of two positions.
   
   ^Range"
  [^Position start ^Position end]
  (vscode.Range. start end))

(defn insert
  "Insert text at a location.
   You can use \r \n or \n in `value `and they will be normalized to the
   current ^TextDocument document.
 
   ^string"
  ([^string value]
   (insert (cursor) value))
  ([^Position location ^string value]
   (. (editor) (edit (fn [^TextEditorEdit editBuilder]
                       (. editBuilder (insert location value)))))
   value))

(defn vs-replace
  "^string"
  [^Position|Range|Selection location ^string value]
  (. (editor) (edit (fn [^TextEditorEdit editBuilder]
                      (. editBuilder (replace location value)))))
  value)

(defn editor-contents
  "Get the text of this document.
   
   ^string"
  []
  (.. (editor) -document getText))

(defn line-contents
  "Returns a text line denoted by the position. Note
   that the returned object is *not* live and changes to the
   document are not reflected.
   
   ^string"
  ([]
   (line-contents (. (cursor) -line)))
  ([^number line]
   (.. (editor) -document (lineAt line) -text)))

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

(defn clone-string [n s]
  (s/join
   (for [_ (range 0 n)] s)))
