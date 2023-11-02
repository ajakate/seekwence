(ns ajakate.seekwence.constants)


(def board-cards
  [["J" "2S" "3S" "4S" "5S" "6S" "7S" "8S" "9S" "J"]
   ["6C" "5C" "4C" "3C" "2C" "AH" "KH" "QH" "10H" "10S"]
   ["7C" "AS" "2D" "3D" "4D" "5D" "6D" "7D" "9H" "QS"]
   ["8C" "KS" "6C" "5C" "4C" "3C" "2C" "8D" "8H" "KS"]
   ["9C" "QS" "7C" "6H" "5H" "4H" "AH" "9D" "7H" "AS"]
   ["10C" "10H" "8C" "7H" "2H" "3H" "KH" "10D" "6H" "2D"]
   ["QC" "9S" "9C" "8H" "9H" "10H" "QH" "QD" "5H" "3D"]
   ["KC" "8S" "10C" "QC" "KC" "AC" "AD" "KD" "4H" "4D"]
   ["AC" "7S" "6S" "5S" "4S" "3S" "2S" "2H" "3H" "5D"]
   ["J" "AD" "KD" "QD" "10D" "9D" "8D" "7D" "6D" "J"]])

(def deck
  ["2S"
   "3S"
   "4S"
   "5S"
   "6S"
   "7S"
   "8S"
   "9S"
   "10S"
   "JS"
   "QS"
   "KS"
   "AS"

   "2H"
   "3H"
   "4H"
   "5H"
   "6H"
   "7H"
   "8H"
   "9H"
   "10H"
   "JH"
   "QH"
   "KH"
   "AH"

   "2D"
   "3D"
   "4D"
   "5D"
   "6D"
   "7D"
   "8D"
   "9D"
   "10D"
   "JD"
   "QD"
   "KD"
   "AD"

   "2C"
   "3C"
   "4C"
   "5C"
   "6C"
   "7C"
   "8C"
   "9C"
   "10C"
   "JC"
   "QC"
   "KC"
   "AC"])

(def teams ["Red", "Green", "Blue"])

(defn cards-to-deal [num-players]
  (case num-players
    2 7
    3 6
    4 6
    6 5
    8 4
    9 4
    10 3
    12 3))
