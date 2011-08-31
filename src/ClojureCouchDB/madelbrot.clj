(ns ClojureCouchDB.madelbrot

  (:import (java.awt Color Dimension)
           (java.awt.image BufferedImage)
           (javax.swing JPanel JFrame)))

(def param {:size 500
            :max-abs-val 100
            :max-iter 2000
            :xmin -1.5
            :ymin -1.0
            :xmax 0.5
            :ymax 1.0})

(defn scale-range [x min max steps]
  (+ min (* x (/ (- max min) steps))))

(defn size-range []
  (range 0 (param :size)))

(defn color-log-scale
  "Skalierung des Iterations-Wertes."
  [n max-n]
  (max (* 255
         (/ (Math/log (* n (/ 255.0 max-n)))
           (Math/log 255)))
    0.0))

(defn color-value
  "Berechnung des Farbwertes."
  [iter]
  (int
    (- 255
      (color-log-scale iter (param :max-iter)))))

(defn mandelbrot-iterations
  "Naive Implementation der Mandelbrot-Iteration.
Enthält Typ-Hinweise für den Compiler."
  [x0 y0 max-abs max-iterations]
  (let [x0 (float x0)
        y0 (float y0)
        max-iter (int max-iterations)]

    (loop [x (float x0)
           y (float y0)
           iter (int 0)]
      (if (== iter max-iter)
        0
        (let [x1 (+ x0 (- (* x x) (* y y)))
              y1 (+ y0 (* (float 2) (* x y)))
              abs (+ (* x1 x1) (* y1 y1))]
          (if (< abs max-abs)
            (recur x1 y1 (+ iter (int 1)))
            iter))))))

(defn mandelbrot-line
  "Berechne eine Zeile der Mandelbrot-Menge.
Achtung das Resultat ist lazy."
  [x]
  (map (fn [pxy]
         (let [y (scale-range
           pxy
           (param :ymin)
           (param :ymax)
           (param :size))]
           (mandelbrot-iterations
             x y
             (param :max-abs-val)
             (param :max-iter))))
    (size-range)))

(def world (ref (vec (repeat (param :size) []))))

(defn make-world-image []
  (let [img (new BufferedImage
    (inc (param :size))
    (inc (param :size))
    BufferedImage/TYPE_INT_ARGB)]
    (doseq [pxy (size-range)]
      (let [line (world pxy)]
        (when (not (empty? line))
          (doseq [pxx (size-range)]
            (.setRGB img
              pxx
              pxy
              (.getRGB
                (Color.
                  (color-value (line pxx))
                  (color-value (line pxx))
                  255)))))))
    img))


(defn repaint-world-img [g]
  (.drawImage g (make-world-image) 0 0 nil))

(def world-agent (agent {}))

(defn mandelbrot-parallel []
  (let [frame (JFrame. "Fractal Multi-Threaded")
        panel (proxy [JPanel] []
      (paintComponent
        [g]
        (proxy-super paintComponent g)
        (repaint-world-img g)
        ))]
    (dosync
      (ref-set world
        (vec (repeat (param :size) []))))

    (.setPreferredSize
      panel
      (Dimension. (param :size) (param :size)))

    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))

    ;; The Meat
    (dorun
      (pmap (fn [pxx]
              (let [x (scale-range
                pxx
                (param :xmin)
                (param :xmax)
                (param :size))
                    newline (vec (mandelbrot-line x)) ]
                (dosync
                  (alter world
                    (fn [state]
                      (assoc state pxx newline))))

                (send-off world-agent
                  (fn [_]
                    (.repaint panel)))))
        (size-range)))

    panel))

(mandelbrot-parallel )

