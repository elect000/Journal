;;; init.el --- Spacemacs Initialization File
;;
;; Copyright (c) 2012-2017 Sylvain Benner & Contributors
;;
;; Author: Sylvain Benner <sylvain.benner@gmail.com>
;; URL: https://github.com/syl20bnr/spacemacs
;;
;; This file is not part of GNU Emacs.
;;
;;; License: GPLv3

;; Without this comment emacs25 adds (package-initialize) here
(package-initialize)

;; Increase gc-cons-threshold, depending on your system you may set it back to a
;; lower value in your dotfile (function `dotspacemacs/user-config')
(setq gc-cons-threshold 100000000)

(defconst spacemacs-version          "0.200.9" "Spacemacs version.")
(defconst spacemacs-emacs-min-version   "24.4" "Minimal version of Emacs.")

(if (not (version<= spacemacs-emacs-min-version emacs-version))
    (error (concat "Your version of Emacs (%s) is too old. "
                   "Spacemacs requires Emacs version %s or above.")
           emacs-version spacemacs-emacs-min-version)
  (load-file (concat (file-name-directory load-file-name)
                     "core/core-load-paths.el"))
  (require 'core-spacemacs)
  (spacemacs/init)
  (configuration-layer/sync)
  (spacemacs-buffer/display-startup-note)
  (spacemacs/setup-startup-hook)
  (require 'server)
  (unless (server-running-p) (server-start)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; 初期表示位置、サイズ
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(setq initial-frame-alist
      '((left   . 100)			; 位置 (X)
	(top    .  50)			; 位置 (Y)
	(width  . 120)			; サイズ(幅)
	(height .  40)			; サイズ(高さ)
	))
(set-face-attribute 'default nil :height 130)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; スクロール
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; adds
(scroll-bar-mode 0)
;; スクロールした際のカーソルの移動行数
(setq scroll-conservatively 1)

;; スクロール開始のマージンの行数
(setq scroll-margin 10)


;; 1 画面スクロール時に重複させる行数
(setq next-screen-context-lines 10)

;; 1 画面スクロール時にカーソルの画面上の位置をなるべく変えない
(setq scroll-preserve-screen-position t)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; 検索、置換時の大文字、小文字の区別
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; オプションの "Ignore Case for Search" で設定可
;;
;; ;; 検索(全般)
;; (setq case-fold-search t)
;;
;; ;; インクリメンタルサーチ
;; (setq isearch-case-fold-search nil)


;; バッファー名の検索
(setq read-buffer-completion-ignore-case t)

;; ファイル名の検索
(setq read-file-name-completion-ignore-case t)


;;;置換(全般)
;; (setq case-replace t)

;; dabbrev 時の置換
(setq dabbrev-case-replace nil)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; 自動作成ファイル
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; backup ファイルオープン時のバックアップ (xxx~)
;; ================================================================

;; 実行の有無
(setq make-backup-files t)

;; 格納ディレクトリーの変更
;;   (対象ディレクトリー . 格納ディレクトリー) のリスト
(setq backup-directory-alist '((".*" . "~/.ehist")))


;; 番号付けによる複数保存
(setq version-control     t)  ;; 実行の有無
(setq kept-new-versions   5)  ;; 最新の保持数
(setq kept-old-versions   1)  ;; 最古の保持数
(setq delete-old-versions t)  ;; 範囲外を削除



;; auto-save 自動保存ファイル (#xxx#)
;; ================================================================

;; ;; 実行の有無
;; (setq auto-save-default nil)

;; ;; 格納ディレクトリーの変更
;; ;;   (対象ファイルのパターン . 保存ファイルパス) のリスト
;; (setq auto-save-file-name-transforms
;;       (append auto-save-file-name-transforms
;; 	      '((".*" "~/tmp/" t))))


;; 保存の間隔
(setq auto-save-timeout 10)	 ;; 秒   (デフォルト : 30)
(setq auto-save-interval 100)	 ;; 打鍵 (デフォルト : 300)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; パッケージ
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(require 'package)

;; MELPAを追加
(add-to-list 'package-archives '("melpa" . "https://melpa.org/packages/") t)

;; MELPA-stableを追加
(add-to-list 'package-archives '("melpa-stable" . "https://stable.melpa.org/packages/") t)

;; Marmaladeを追加
(add-to-list 'package-archives  '("marmalade" . "http://marmalade-repo.org/packages/") t)

;; Orgを追加
(add-to-list 'package-archives '("org" . "http://orgmode.org/elpa/") t)
 (package-initialize)
;; because of el-get


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; auto-complete
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(when (require 'auto-complete-config nil t)
  (ac-config-default))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; ロードパスの設定
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(add-to-list 'load-path 
	     "~/.emacs.d/site-lisp")
(add-to-list 'load-path 
	     "~/.emacs.d/elpa")
;; ;; load-path に登録されたディレクトリーを subdir 扱い
;; ;;   注:アクセス件でエラーになりやすい
;; (normal-top-level-add-subdirs-to-load-path)


;; ;; キー設定ファイルのロード
;; (load "my-keyset-light")



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; プログラミング
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; 共通
;; ================================================================

;; 左端(文字の前)ではインデント、それ以外はタブの挿入
(setq tab-always-indent nil)
(setq c-tab-always-indent nil)

;; 空白を一度に削除
(if (fboundp 'global-hungry-delete-mode)
    (global-hungry-delete-mode 1))

;; 改行時などに自動でインデント 
;;   (C-j と C-m の入れ替え)
(if (fboundp 'electric-indent-mode)
    (electric-indent-mode 0))

;; 特定の文字を入力すると自動で改行、インデント
;; (electric-layout-mode 1)


;; C 系共通
;; ================================================================

(defun my-all-cc-mode-init ()
  ;; C 系(cc-mode を継承した)モード共通の設定を記述

  ;; 空白などを一度に削除
  (c-toggle-hungry-state 1)

  ;; 改行時などで自動インデント
  ;; (c-toggle-electric-state 1)
  ;; 
  ;; ";", "}" などを入力したときに自動改行
  ;; 自動インデントも一緒に ON になる
  ;; (c-toggle-auto-newline 1)

  )
(add-hook 'c-mode-common-hook 'my-all-cc-mode-init)
(add-hook 'c-mode-hook 'c-turn-on-eldoc-mode)


;; C, C++
;; ================================================================

(autoload 'vs-set-c-style "vs-set-c-style"
  "Set the current buffer's c-style to Visual Studio like style. ")

(defun my-c-c++-mode-init ()
  ;; C, C++ 用の設定を記述
  

  ;; Visual Studio 風の設定
  ;; (vs-set-c-style)
  )
(add-hook 'c-mode-hook 'my-c-c++-mode-init)
(add-hook 'c++-mode-hook 'my-c-c++-mode-init)


;; .h でも C++
(add-to-list 'auto-mode-alist '("\\.h\\'" . c++-mode))

;; compile ;; 

(require 'compile)

(defvar yel-compile-auto-close t
  "* If non-nil, a window is automatically closed after (\\[compile]).")

(defadvice compile (after compile-aftercheck
                          activate compile)
  "Adds a funcion of windows auto-close."
  (let ((proc (get-buffer-process "*compilation*")))
    (if (and proc yel-compile-auto-close)
        (set-process-sentinel proc 'yel-compile-teardown))))

(defun yel-compile-teardown (proc status)
  "Closes window automatically, if compile succeed."
  (let ((ps (process-status proc)))
    (if (eq ps 'exit)
        (if (eq 0 (process-exit-status proc))
            (progn
              (delete-other-windows)
              (kill-buffer "*compilation*")
              (message "---- Compile Success ----")
              )
          (message "Compile Failer")))
    (if (eq ps 'signal)
        (message "Compile Abnormal end"))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; Tips
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ビープ音禁止
(setq ring-bell-function 'ignore)


;; スタート画面(メッセージ)を表示しない
(setq inhibit-startup-screen t)


;; 選択領域を削除キーで一括削除
(delete-selection-mode t)

;; shift + 矢印キーで領域選択
(if (fboundp 'pc-selection-mode)
    (pc-selection-mode))


;; 行頭で kill-line (C-k) で行全体でカット
(setq kill-whole-line t)

;; 読み取り専用バッファーでもカット系でコピー可能
(setq kill-read-only-ok t)


;; ediff 時にフレームを使わない
(setq ediff-window-setup-function 'ediff-setup-windows-plain)


;; png, jpg などのファイルを画像として表示
(setq auto-image-file-mode t)



;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; others
;;;;;;;;;;;;;;;;;;;;;;;;;;

;; load path
(add-to-list 'load-path
	     (expand-file-name "~/.emacs.c/elpa/yasnippet"))

;; Color
(if window-system (progn
;;    (set-background-color "Black")
;;   (set-foreground-color "LightGray")
;;   (set-cursor-color "Gray")
    (set-frame-parameter nil 'alpha 100) ;透明度
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; yasnippet
(require 'yasnippet)
(add-to-list 'load-path "~/.emacs.d/elpa/yasnippet-20170108.1830")
(yas-global-mode 1)

;; egg
(require 'egg)

;; twitting mode
(require 'twittering-mode)
(setq twittering-use-master-password t)
(setq twittering-initial-timeline-spec-string
      '(":home"
        ":replies"
        ":favorites"
        ":direct_messages"
        ":search/emacs/"
        "user_name/list_name"))

(setq twittering-icon-mode t)                ; Show icons
(setq twittering-timer-interval 15)         ; Update your timeline each 300 seconds (5 minutes) ->15
(setq twittering-url-show-status nil)        ; Keeps the echo area from showing all the http processes


;; line
(global-linum-mode t)

;; org-mode
(require 'ox-latex)
(add-to-list 'org-latex-classes
	     '("koma-article"
	       "\\documentclass{scrartcl}
		\\usepackage[utf8]{inputenc}
		\\usepackage[dvipdfmx]{graphicx}
		\\usepackage[dvipdfmx]{color}
		\\usepackage[backend=biber,bibencoding=utf8]{biblatex}
		\\usepackage{url}
		\\usepackage{indentfirst}
		\\usepackage[normalem]{ulem}
		\\usepackage[dvipdfmx]{hyperref}
		\\usepackage{longtable}
		\\usepackage{minted}
		\\usepackage{fancyvrb}
		\\usepackage[top=25truemm,bottom=25truemm,left=25truemm,right=25truemm]{geometry}
		[NO-DEFAULT-PACKAGES]
		[EXTRA]"
	       ("\\section{%s}"."\\section*{%s}")
	       ("\\subsection{%s}"."\\subsection*{%s}")
	       ("\\subsubsection{%s}"."\\subsubsection*{%s}")
	       ("\\paragraph{%s}"."\\paragraph*{%s}")
	       ("\\subparagraph{%s}"."\\subparagraph{%s}")))
(add-to-list 'org-latex-classes
	     '("elect-book"
	       "\\documentclass[a5j,12pt]{tarticle}
		\\usepackage[utf8]{inputenc}
		\\usepackage{pxrubrica}
		\\usepackage[dvipdfmx]{graphicx}
		\\usepackage[dvipdfmx]{color}
		\\usepackage{indentfirst}
		\\usepackage[normalem]{ulem}
		\\usepackage[dvipdfmx]{hyperref}
		\\usepackage{longtable}
		[NO-DEFAULT-PACKAGES]
		[EXTRA]"))
(setq org-agenda-files '("~/GitHub/Tasks/daily.org"))

(require 'ox-latex)
(setq org-latex-listings 'minted)
(setq org-latex-minted-options	
     '(("frame" "lines") ("linenos=true") ("obeytabs") ("tabsize=4")))

(setq word-wrap nil)
;; ditaa.jar の path を設定
(setq org-ditaa-jar-path "/usr/share/ditaa/ditaa.jar")
;; コードを評価するとき尋ねない
(setq org-confirm-babel-evaluate nil)
;; 有効にする言語 デフォルトでは elisp のみ
(org-babel-do-load-languages
 'org-babel-load-languages
 '((ditaa . t)))

(setq org-latex-hyperref-template t)
(setq org-src-fontify-natively t)

;; smartparens
(require 'smartparens-config)
(smartparens-global-mode t)
(sp-pair "*" "*")

;; rainbow-delimiters-mode
;; rainbow-delimiters を使うための設定
(require 'rainbow-delimiters)
(add-hook 'prog-mode-hook 'rainbow-delimiters-mode)

;; 括弧の色を強調する設定
(require 'cl-lib)
(require 'color)
(defun rainbow-delimiters-using-stronger-colors ()
  (interactive)
  (cl-loop
   for index from 1 to rainbow-delimiters-max-face-count
   do
   (let ((face (intern (format "rainbow-delimiters-depth-%d-face" index))))
    (cl-callf color-saturate-name (face-foreground face) 30))))
(add-hook 'emacs-startup-hook 'rainbow-delimiters-using-stronger-colors)

;; helm mode
(require 'helm-config)
(helm-mode 1)

;; キーに割り当てる
(global-set-key (kbd "M-<right>") 'tabbar-forward-tab)
(global-set-key (kbd "M-<left>") 'tabbar-backward-tab)

;; toggle-trunslate-line
(toggle-truncate-lines t)

;; slime
(setq inferior-lisp-program "/usr/bin/clisp") 		;; !!! you must change it !!!
;;(setq inferior-lisp-program "/usr/bin/sbcl") 		;; !!! you must change it !!!

;; SBCLをデフォルトのCommon Lisp処理系に設定
(setq inferior-lisp-program "clisp")
;;(setq inferior-lisp-program "sbcl")


;; SLIMEのロード
(require 'slime)
(slime-setup '(slime-repl slime-fancy slime-banner)) 

;; ctable
(require 'ctable)


;; path
(setq exec-path-from-shell-arguments '("-l"))
(setq exec-path-from-shell-check-startup-files nil)
(setq exec-path-from-shell-arguments '("-l"))
(exec-path-from-shell-initialize)

;; stylus 
(require 'stylus-mode)


;; image+
(eval-after-load 'image '(require 'image+))
(eval-after-load 'image+
  `(when (require 'hydra nil t)
     (defhydra imagex-sticky-binding (global-map "C-x C-l")
       "Manipulating Image"
       ("+" imagex-sticky-zoom-in "zoom in")
       ("-" imagex-sticky-zoom-out "zoom out")
       ("M" imagex-sticky-maximize "maximize")
       ("O" imagex-sticky-restore-original "restore original")
       ("S" imagex-sticky-save-image "save file")
       ("r" imagex-sticky-rotate-right "rotate right")
       ("l" imagex-sticky-rotate-left "rotate left"))))
;; reload file
(auto-revert-mode t)
(auto-image-file-mode t)

;; sr-
(require 'sr-speedbar) 
(setq sr-speedbar-right-side nil) 
(setq sr-speedbar-width-x 15)
(global-set-key (kbd "<f5>") 'sr-speedbar-toggle)

;; imenu-list
(require 'imenu)
(setq imenu-auto-rescan t)


;; clojure ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(require 'clojure-mode)
(add-hook 'clojure-mode-hook #'subword-mode)
(add-hook 'clojure-mode-hook #'cider-mode)


(require 'cider)
(add-hook 'cider-mode-hook #'clj-refactor-mode)
(add-hook 'cider-mode-hook #'company-mode)
(add-hook 'cider-mode-hook #'eldoc-mode)
(add-hook 'cider-repl-mode-hook #'company-mode)
(add-hook 'cider-repl-mode-hook #'eldoc-mode)
(add-hook 'cider-mode-hook #'clj-refactor-mode)

(require 'ac-cider)
(add-hook 'cider-mode-hook 'ac-cider-setup)
(add-hook 'cider-repl-mode-hook 'ac-cider-setup)


;; flycheck ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(require 'flycheck)
(global-flycheck-mode)
(with-eval-after-load 'flycheck
  (flycheck-pos-tip-mode))


;; file make

;;; ;;; find-fileでディレクトリが無ければ作る
(defun make-directory-unless-directory-exists ()
  (let (
         (d (file-name-directory buffer-file-name))
       )
    (unless (file-directory-p d)
      (when (y-or-n-p "No such directory: make directory?")
        (make-directory d t))
      )
    )
  nil
)
(add-hook 'find-file-not-found-hooks
          'make-directory-unless-directory-exists)


;; octave
(autoload 'octave-mode "octave-mod" nil t)
(setq auto-mode-alist
           (cons '("\\.m$" . octave-mode) auto-mode-alist))
(add-hook 'octave-mode-hook
               (lambda ()
                 (abbrev-mode 1)
                 (auto-fill-mode 1)
                 (if (eq window-system 'x)
                     (font-lock-mode t))))

;; marmalade
(require 'meghanada)
(add-hook 'java-mode-hook
          (lambda ()
            ;; meghanada-mode on
            (meghanada-mode t)
            (add-hook 'before-save-hook 'meghanada-code-beautify-before-save)))

;; shell init error
;;https://github.com/syl20bnr/spacemacs/issues/3920

;; rainbow
(require 'rainbow-delimiters)
(add-hook 'prog-mode-hook 'rainbow-delimiters-mode)
(require 'cl-lib)
(require 'color)
(defun rainbow-delimiters-using-stronger-colors ()
  (interactive)
  (cl-loop
   for index from 1 to rainbow-delimiters-max-face-count
   do
   (let ((face (intern (format "rainbow-delimiters-depth-%d-face" index))))
    (cl-callf color-saturate-name (face-foreground face) 30))))
(add-hook 'emacs-startup-hook 'rainbow-delimiters-using-stronger-colors)




(load "graphviz-dot-mode.el")
(add-hook 'graphviz-dot-mode-hook (lambda () (local-set-key [f5] "\C-x\C-s\C-cc\C-m\C-cp")))
;;

;; emacs-mozc
(require 'mozc)
(set-language-environment "Japanese")
(setq default-input-method "japanese-mozc")
(require 'mozc-popup)
(setq mozc-candidate-style 'popup) ; select popup style.
(require 'ac-mozc)
(define-key ac-mode-map (kbd "C-c C-SPC") 'ac-complete-mozc)

(prefer-coding-system 'utf-8)
(setq coding-system-for-read 'utf-8)
(setq coding-system-for-write 'utf-8)
