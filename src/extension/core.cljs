(ns extension.core
  (:require [extension.lib :refer [register-command
                                   message
                                   editor
                                   selection 
                                   set-selection
                                   remove-selection
                                   execute-command
                                   cursor
                                   following-char
                                   preceding-char
                                   in?]]))

(defn exchange-point-and-mark
  "Put the selection start where cursor is now, and cursor where the 
   selection start is now.
   This command works even when the selection is not active, 
   and it reactivates the mark."
  []
  (let [selection (selection)]
    (when (and (editor) (not (. selection -isEmpty)))
      (let [end (. selection -end)
            start (. selection -start)
            position (. selection -active)]
        (if (== start position)
          (set-selection start end)
          (set-selection end start))))))

(defn navigate-sexp [select direction]
  (let [sel (selection)
        selection-start (if (.isBefore (. sel -end)  (. sel -start))
                          (if (= direction :forward) (. sel -end) (. sel -start))
                          (if (= direction :forward) (. sel -start) (. sel -end)))
        update-selection #(let [cursor-pos (cursor)]
                            (when (and select cursor-pos)
                              (if (. sel -isEmpty)
                                (set-selection (. sel -active) cursor-pos)
                                (set-selection selection-start cursor-pos))))
        brackets (if (= direction :forward) ["(" "[" "{"] [")" "]" "}"])
        char-near-to (if (= direction :forward) (following-char) (preceding-char))
        nav-char-cmd (if (= direction :forward) "cursorRight" nil)
        nav-word-cmd (if (= direction :forward) "cursorWordRight" "cursorWordLeft")]
    (if (in? brackets char-near-to)
      (do
        (remove-selection)
        (. (execute-command "editor.action.jumpToBracket")
           then #(if nav-char-cmd
                   (. (execute-command nav-char-cmd)
                      then update-selection)
                   (update-selection))))
      (. (execute-command nav-word-cmd)
         then update-selection))))

(defn forward-sexp [select]
  (navigate-sexp select :forward))

(defn backward-sexp [select]
  (navigate-sexp select :backward))

(defn activate-advanced-navigation []
  (message "advanced-navigation activated"))

(defn activate [context]
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.forwardSexp" #(forward-sexp false))))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.backwardSexp" #(backward-sexp false))))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.forwardSexpSelect" #(forward-sexp true))))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.backwardSexpSelect" #(backward-sexp true))))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.exchangePointAndMark" #'exchange-point-and-mark)))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.activate" #'activate-advanced-navigation))))

(defn deactivate [])

(defn reload
  []
  (.log js/console "Reloading...")
  (js-delete js/require.cache (js/require.resolve "./extension")))

(def exports #js {:activate activate
                  :deactivate deactivate})