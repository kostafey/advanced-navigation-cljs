(ns extension.core
  (:require [extension.lib :refer [register-command
                                   message
                                   editor
                                   selection set-selection]]))

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

(defn activate-advanced-navigation []
  (message "advanced-navigation activated"))

(defn activate [context]
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