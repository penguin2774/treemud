;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions for ascii color escapes.

(ns treemud.utils.color
  ;; (:use [clojure.string :only [re-split]])
  )

;; Text attributes
;; 0	All attributes off
;; 1	Bold on
;; 4	Underscore (on monochrome display adapter only)
;; 5	Blink on
;; 7	Reverse video on
;; 8	Concealed on
 
;; Foreground colors
;; 30	Black
;; 31	Red
;; 32	Green
;; 33	Yellow
;; 34	Blue
;; 35	Magenta
;; 36	Cyan
;; 37	White
 
;; Background colors
;; 40	Black
;; 41	Red
;; 42	Green
;; 43	Yellow
;; 44	Blue
;; 45	Magenta
;; 46	Cyan
;; 47	White

(def ^{:doc "True if the user in context can accept color escapes." :dynamic true} *colors?* false)
;; These are duplicates from the smaug color system. For compatibilty.
(let [mod-escape   {:reset                0
		    :bold                 1
		    :underline            4
		    :blink                5
		    :italic               6
		    :reverse              7}
      fg-escape    {:black                30
		    :red                  31
		    :green                32
		    :yellow               33
		    :blue                 34
		    :magenta              35
		    :cyan                 36
		    :white                37}
      bg-escape    {:black                40
		    :red                  41
		    :green                42
		    :yellow               43
		    :blue                 44
		    :magenta              45
		    :cyan                 46
		    :white                47}]

;; escape    @
;; reset     !
;; bold      #
;; underline $
;; blink     %
;; italic    ^
;; reverse   &
;; forground:
;; black     k
;; red       r
;; green     g
;; yellow    y
;; blue      b
;; magenta   m
;; cyan      c
;; white     w
;; background:
;; black     K
;; red       R
;; green     H
;; yellow    Y
;; blue      B
;; magenta   M
;; cyan      C
;; white     W
		    
  (defn code
    "Produces a ansii terminal escape code, 
Colors must be givien in order, but an excape can be given in place of colors,
for example: (code :green :blink) is valid and so is (code :reset).
fg - forground color
bg - background color
mods - mod in question"
    [fg & [bg & mods]]
    (if *colors?*
      (let [mods (concat (map mod-escape mods) (if fg [(or (fg-escape fg)
							   (mod-escape fg))] nil) (if bg  [(or (bg-escape bg) (mod-escape bg))] nil))
	    mod-str (apply str (interpose ";" mods))]
		    
	
	(format "%c[%sm" (int 0x1b) mod-str))
      "")))

(defn color-str 
  "Makes a color string from args in the sequence
fg [bg reset] string. Works the same as code so mods can come at any time, but sould be the last in the sequence."
  [& mods-str]
  (str (apply code (take-while keyword? mods-str)) (apply str (drop-while keyword? mods-str)) (code :reset)))


;; (defn- get-color-from-escape [color-str]
;;   (let [c (second color-str)
;; 	o (if (or (= c \{)
;; 		  (= c \}))
;; 	    (nth color-str 2))
;; 	escape (if o
;; 		 ((color-escapes c) o)
;; 		 (color-escapes c))]
;;     (println c o escape)
;;     (cond 
;;      (= c \&)
;;      "&"
;;      (vector? escape)
;;      ((color-codes (first escape)) (second escape))
;;      true
;;      (color-codes escape))))
	
	
    

;; (def color-regex #"&[&iIvVuUdxOczgGPrbwYCpRBW]|&[\{\}][xOczgGPrbwYCpRBW]")


;; (defn color-escape [string]
;;   (let [sb (StringBuffer.)]
;;   (loop [match (re-matcher color-regex string)]    
;;     (if (.find match)
;;       (do (.appendReplacement match sb (get-color-from-escape (re-groups match)))
;; 	  (recur match))
;;       (.toString sb)))))

;; (defmacro mud-str [& args]
;;   `(str
;;     ~@(for [i args]
;; 	(cond 
;; 	 (keyword? i)
;; 	 (color-codes i)
;; 	 (vector? i)
;; 	 ((color-codes (first i))
	 


;; Esc[Line;ColumnH
;; Esc[Line;Columnf 	Cursor Position:
;; Moves the cursor to the specified position (coordinates).
;; If you do not specify a position, the cursor moves to the home position at the upper-left corner of the screen (line 0, column 0). This escape sequence works the same way as the following Cursor Position escape sequence.
;; Esc[ValueA 	Cursor Up:
;; Moves the cursor up by the specified number of lines without changing columns. If the cursor is already on the top line, ANSI.SYS ignores this sequence.
;; Esc[ValueB 	Cursor Down:
;; Moves the cursor down by the specified number of lines without changing columns. If the cursor is already on the bottom line, ANSI.SYS ignores this sequence.
;; Esc[ValueC 	Cursor Forward:
;; Moves the cursor forward by the specified number of columns without changing lines. If the cursor is already in the rightmost column, ANSI.SYS ignores this sequence.
;; Esc[ValueD 	Cursor Backward:
;; Moves the cursor back by the specified number of columns without changing lines. If the cursor is already in the leftmost column, ANSI.SYS ignores this sequence.
;; Esc[s 	Save Cursor Position:
;; Saves the current cursor position. You can move the cursor to the saved cursor position by using the Restore Cursor Position sequence.
;; Esc[u 	Restore Cursor Position:
;; Returns the cursor to the position stored by the Save Cursor Position sequence.
;; Esc[2J 	Erase Display:
;; Clears the screen and moves the cursor to the home position (line 0, column 0).
;; Esc[K 	Erase Line:
;; Clears all characters from the cursor position to the end of the line (including the character at the cursor position).
;; Esc[Value;...;Valuem 	Set Graphics Mode:
;; Calls the graphics functions specified by the following values. These specified functions remain active until the next occurrence of this escape sequence. Graphics mode changes the colors and attributes of text (such as bold and underline) displayed on the screen.
 
;; Text attributes
;; 0	All attributes off
;; 1	Bold on
;; 4	Underscore (on monochrome display adapter only)
;; 5	Blink on
;; 7	Reverse video on
;; 8	Concealed on
 
;; Foreground colors
;; 30	Black
;; 31	Red
;; 32	Green
;; 33	Yellow
;; 34	Blue
;; 35	Magenta
;; 36	Cyan
;; 37	White
 
;; Background colors
;; 40	Black
;; 41	Red
;; 42	Green
;; 43	Yellow
;; 44	Blue
;; 45	Magenta
;; 46	Cyan
;; 47	White
 
;; Parameters 30 through 47 meet the ISO 6429 standard.
 
;; Esc[=Valueh 	Set Mode:
;; Changes the screen width or type to the mode specified by one of the following values:
 
;; Screen resolution
;; 0	40 x 25 monochrome (text)
;; 1	40 x 25 color (text)
;; 2	80 x 25 monochrome (text)
;; 3	80 x 25 color (text)
;; 4	320 x 200 4-color (graphics)
;; 5	320 x 200 monochrome (graphics)
;; 6	640 x 200 monochrome (graphics)
;; 7	Enables line wrapping
;; 13	320 x 200 color (graphics)
;; 14	640 x 200 color (16-color graphics)
;; 15	640 x 350 monochrome (2-color graphics)
;; 16	640 x 350 color (16-color graphics)
;; 17	640 x 480 monochrome (2-color graphics)
;; 18	640 x 480 color (16-color graphics)
;; 19	320 x 200 color (256-color graphics)
 
 
;; Esc[=Valuel 	Reset Mode:
;; Resets the mode by using the same values that Set Mode uses, except for 7, which disables line wrapping
;; (the last character in this escape sequence is a lowercase L).
;; Esc[Code;String;...p 	Set Keyboard Strings:
;; Redefines a keyboard key to a specified string.
 
;; The parameters for this escape sequence are defined as follows:
 
;; Code is one or more of the values listed in the following table. These values represent keyboard keys and key combinations. When using these values in a command, you must type the semicolons shown in this table in addition to the semicolons required by the escape sequence. The codes in parentheses are not available on some keyboards. ANSI.SYS will not interpret the codes in parentheses for those keyboards unless you specify the /X switch in the DEVICE command for ANSI.SYS.
 
;; String is either the ASCII code for a single character or a string contained in quotation marks. For example, both 65 and "A" can be used to represent an uppercase A.
 
;; IMPORTANT: Some of the values in the following table are not valid for all computers. Check your computer's documentation for values that are different.
 
;; Key 	Code 	SHIFT+code 	CTRL+code 	ALT+code
;; F1 	0;59 	0;84 	0;94 	0;104
;; F2 	0;60 	0;85 	0;95 	0;105
;; F3 	0;61 	0;86 	0;96 	0;106
;; F4 	0;62 	0;87 	0;97 	0;107
;; F5 	0;63 	0;88 	0;98 	0;108
;; F6 	0;64 	0;89 	0;99 	0;109
;; F7 	0;65 	0;90 	0;100 	0;110
;; F8 	0;66 	0;91 	0;101 	0;111
;; F9 	0;67 	0;92 	0;102 	0;112
;; F10 	0;68 	0;93 	0;103 	0;113
;; F11 	0;133 	0;135 	0;137 	0;139
;; F12 	0;134 	0;136 	0;138 	0;140
;; HOME (num keypad) 	0;71 	55 	0;119 	--
;; UP ARROW (num keypad) 	0;72 	56 	(0;141) 	--
;; PAGE UP (num keypad) 	0;73 	57 	0;132 	--
;; LEFT ARROW (num keypad) 	0;75 	52 	0;115 	--
;; RIGHT ARROW (num keypad)	0;77 	54 	0;116 	--
;; END (num keypad) 	0;79 	49 	0;117 	--
;; DOWN ARROW (num keypad) 	0;80 	50 	(0;145) 	--
;; PAGE DOWN (num keypad) 	0;81 	51 	0;118 	--
;; INSERT (num keypad) 	0;82 	48 	(0;146) 	--
;; DELETE (num keypad) 	0;83 	46 	(0;147) 	--
;; HOME 	(224;71) 	(224;71) 	(224;119) 	(224;151)
;; UP ARROW 	(224;72) 	(224;72) 	(224;141) 	(224;152)
;; PAGE UP 	(224;73) 	(224;73) 	(224;132) 	(224;153)
;; LEFT ARROW 	(224;75) 	(224;75) 	(224;115) 	(224;155)
;; RIGHT ARROW 	(224;77) 	(224;77) 	(224;116) 	(224;157)
;; END 	(224;79) 	(224;79) 	(224;117) 	(224;159)
;; DOWN ARROW 	(224;80) 	(224;80) 	(224;145) 	(224;154)
;; PAGE DOWN 	(224;81) 	(224;81) 	(224;118) 	(224;161)
;; INSERT 	(224;82) 	(224;82) 	(224;146) 	(224;162)
;; DELETE 	(224;83) 	(224;83) 	(224;147) 	(224;163)
;; PRINT SCREEN 	-- 	-- 	0;114 	--
;; PAUSE/BREAK 	-- 	-- 	0;0 	--
;; BACKSPACE 	8 	8 	127 	(0)
;; ENTER 	13 	-- 	10 	(0
;; TAB 	9 	0;15 	(0;148) 	(0;165)
;; NULL 	0;3 	-- 	-- 	--
;; A 	97 	65 	1 	0;30
;; B 	98 	66 	2 	0;48
;; C 	99 	66 	3 	0;46
;; D 	100 	68 	4 	0;32
;; E 	101 	69 	5 	0;18
;; F 	102 	70 	6 	0;33
;; G 	103 	71 	7 	0;34
;; H 	104 	72 	8 	0;35
;; I 	105 	73 	9 	0;23
;; J 	106 	74 	10 	0;36
;; K 	107 	75 	11 	0;37
;; L 	108 	76 	12 	0;38
;; M 	109 	77 	13 	0;50
;; N 	110 	78 	14 	0;49
;; O 	111 	79 	15 	0;24
;; P 	112 	80 	16 	0;25
;; Q 	113 	81 	17 	0;16
;; R 	114 	82 	18 	0;19
;; S 	115 	83 	19 	0;31
;; T 	116 	84 	20 	0;20
;; U 	117 	85 	21 	0;22
;; V 	118 	86 	22 	0;47
;; W 	119 	87 	23 	0;17
;; X 	120 	88 	24 	0;45
;; Y 	121 	89 	25 	0;21
;; Z 	122 	90 	26 	0;44
;; 1 	49 	33 	-- 	0;120
;; 2 	50 	64 	0 	0;121
;; 3 	51 	35 	-- 	0;122
;; 4 	52 	36 	-- 	0;123
;; 5 	53 	37 	-- 	0;124
;; 6 	54 	94 	30 	0;125
;; 7 	55 	38 	-- 	0;126
;; 8 	56 	42 	-- 	0;126
;; 9 	57 	40 	-- 	0;127
;; 0 	48 	41 	-- 	0;129
;; - 	45 	95 	31 	0;130
;; = 	61 	43 	--- 	0;131
;; [ 	91 	123 	27 	0;26
;; ] 	93 	125 	29 	0;27
;; 	92 	124 	28 	0;43
;; ; 	59 	58 	-- 	0;39
;; ' 	39 	34 	-- 	0;40
;; , 	44 	60 	-- 	0;51
;; . 	46 	62 	-- 	0;52
;; / 	47 	63 	-- 	0;53
;; ` 	96 	126 	-- 	(0;41)
;; ENTER (keypad) 	13 	-- 	10 	(0;166)
;; / (keypad) 	47 	47 	(0;142) 	(0;74)
;; * (keypad) 	42 	(0;144) 	(0;78) 	--
;; - (keypad) 	45 	45 	(0;149) 	(0;164)
;; + (keypad) 	43 	43 	(0;150) 	(0;55)
;; 5 (keypad) 	(0;76) 	53 	(0;143) 	--

 
