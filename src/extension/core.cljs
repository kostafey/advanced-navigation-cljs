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
                                   in?
                                   tab-groups
                                   show-text-document
                                   show-text-document-by-uri
                                   close-tab
                                   line-contents
                                   position
                                   vs-replace
                                   vs-range
                                   goto-char]]))

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

(defn- get-tabs-list
  "^Tab[]"
  [^TabGroup tab-group]
  (->>
   (. tab-group -tabs)
   (filter
    (fn [t] (not (undefined? (. t -input)))))))

(defn- get-tab-uri-list
  "^Uri[]"
  [^TabGroup tab-group]
  (->> (get-tabs-list tab-group)
       (map
        (fn [t] (.. t -input -uri)))))

(defn swap-tab-groups
  "Swap 2 tab groups."
  []
  (let [active-document (. (editor) -document)
        groups (tab-groups)]
    (when (> (count groups) 1)
      (let [first-group (first groups)
            first-tabs-list (get-tabs-list first-group)
            first-tab-uri-list (get-tab-uri-list first-group)
            first-view (. first-group -viewColumn)
            second-group (second groups)
            second-tabs-list (get-tabs-list second-group)
            second-tab-uri-list (get-tab-uri-list second-group)
            second-view (. second-group -viewColumn)
            target-active-group (if (. first-group -isActive)
                                  second-view
                                  first-view)
            active-tabs (atom [])
            move-tabs (fn [tab-uri-list view]
                       (fn [^Tab tab]
                        (when (. tab -activeTab)
                          (swap! active-tabs conj tab))
                        (let [^Uri uri (.. tab -input -uri)]
                          (when-not (in? (map (fn [^Uri uri] (. uri -fsPath))
                                              tab-uri-list)
                                         (. uri -fsPath))
                            (show-text-document-by-uri uri {:viewColumn view})
                            (close-tab tab)))))]
        (mapv (move-tabs second-tab-uri-list second-view) first-tabs-list)
        (mapv (move-tabs first-tab-uri-list first-view) second-tabs-list)
        (mapv (fn [tab]
                (show-text-document-by-uri (.. tab -input -uri) {}))
              @active-tabs)
        (show-text-document active-document 
                            {:viewColumn target-active-group})))))

(defn just-one-space
  "Delete all spaces and tabs around point, leaving one space."
  []
  (let [contents (line-contents)
        pos (. (cursor) -character)
        line (. (cursor) -line)
        before (subs contents 0 pos)
        after (subs contents pos (count contents))
        trimmed-str (str (.replace before #"\s+$" "") " "
                         (.replace after #"^\s+" ""))]
    (vs-replace (vs-range (position line 0)
                          (position line (count contents)))
                trimmed-str)
    (goto-char (position
                line (+ 1 (count (.replace before #"\s+$" "")))))))

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
            "advanced-navigation.swapTabGroups" #'swap-tab-groups)))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.activate" #'activate-advanced-navigation)))
  (. context.subscriptions
     (push (register-command
            "advanced-navigation.justOneSpace" #'just-one-space))))

(defn deactivate [])

(defn reload
  []
  (.log js/console "Reloading...")
  (js-delete js/require.cache (js/require.resolve "./extension")))

(def exports #js {:activate activate
                  :deactivate deactivate})