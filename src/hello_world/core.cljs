(ns hello-world.core)

(declare asset move-chara move-to-kinoko-kusa move-type-2
         move-to-player all add-chara spy reset hit chara-chara-hit
         clear-display draw-status defeat draw-string-solid
         chara-hit-kusa chara-hit-kinoko chara-unk-hit chara-tama-hit
         draw-border move-player victory game-over-message
         seed character? gpin)

(def $field_w 640)
(def $field_h 480)
(def $scene)
(def $ctx)
(def $browser2game {
                    "z" :z,
                    "Z" :z,
                    "ArrowRight" :right,
                    "ArrowLeft" :left,
                    "ArrowUp" :up,
                    "ArrowDown" :down
                    })
(def $frame 0)
(def $game-state nil)

(def $keystate {:right false, :left false, :up false, :down false, :z false})

(defmulti entity-update (fn [obj gs] (obj :class)))

(def seed 1)
(defn random [n]
  (let [x (* (Math/sin seed) 10000)
        r (- x (Math/floor x))]
    (set! seed (inc seed))
    (Math/floor (* n r))))
;; (defn random [n]
;;   (Math/floor (* n (Math/random))))

(defn make-entity [params]
  (let [entity (merge {:id nil, :x 0, :y 0,
                       :dx 1, :dy 1, :dir 0, :dir_time 0,
                       :update_frame 0, :state 0, :hp 0,
                       :move_type 0, :goal_x 0, :goal_y 0,
                       :explode_cnt 0, :big 0}
                      params)]
    (merge entity {:hit_box { :left 0, :right (entity :width), :top 0, :bottom (entity :height) },
                   :width (.-width (entity :id)),
                   :height (.-height (entity :id))}
           params)
  ))

(defmulti draw :class)
(defmethod draw :default [obj]
  (assert (map? obj))
  (assert (not (nil? (obj :id))))
  (assert (number? (obj :x)))
  (assert (number? (obj :y)))

  (.drawImage $ctx (obj :id) (obj :x) (obj :y)))

;;   getCenter() {
;;     return [this.x + this.width / 2,
;;             this.y + this.height / 2];
;;   }
(defn get-center [entity]
  (list (/ (+ (entity :x) (entity :width)) 2)
        (/ (+ (entity :y) (entity :height)) 2)))


(defn move-to [obj tx ty]
  (cond
    (> (obj :x) tx) (update obj :x dec)
    (< (obj :x) tx) (update obj :x inc)
    (> (obj :y) ty) (update obj :y dec)
    (< (obj :y) ty) (update obj :y inc)
    :else obj))

;;   collide(other) {
;;     if (this.state != 0 || other.state != 0)
;;       return;

;;     var a = this.hitBox(), b = other.hitBox();

;;     return (this.x + a.right > other.x + b.left &&
;;             this.x + a.left < other.x + b.right &&
;;             this.y + a.bottom > other.y + b.top &&
;;             this.y + a.top < other.y + b.bottom);
;;   }

(defmulti hit-box :class)
(defmethod hit-box :default [entity]
  (entity :hit_box))

(defn collide [entity other]
  (cond
    (or (not= 0 (entity :state))
        (not= 0 (other :state)))
    false

    :else
    (let [a (hit-box entity)
          b (hit-box other)]
      (and (> (+ (entity :x) (a :right))  (+ (other :x) (b :left)))
           (< (+ (entity :x) (a :left))   (+ (other :x) (b :right)))
           (> (+ (entity :y) (a :bottom)) (+ (other :y) (b :top)))
           (< (+ (entity :y) (a :top))    (+ (other :y) (b :bottom)))))))

(defn check-hp [chara]
  (if (character? chara)
    (if (and (zero? (chara :state)) (<= (chara :hp) 0))
      (do
        (.play (asset "explode"))
        (update chara :state (constantly 2)))
      chara)
    chara))

(defmethod draw :gpin [gpin]
  (if (= 2 (gpin :state))
    (let [[x y] (get-center gpin)]
      (.drawImage $ctx (asset "gpin_explode") (- x 32) (- y 32)))
    (.drawImage $ctx (gpin :id) (gpin :x) (gpin :y))))

(defmethod hit-box :gpin [entity]
  (if (= 1 (entity :big))
    { :left 16, :right 52, :top 12, :bottom 52 }
    { :left 8, :right 26, :top 6, :bottom 26 }))

(defmethod hit-box :mukku [entity]
  (if (= 1 (entity :big))
    { :left 16, :right 48, :top 12, :bottom 52 }
    { :left 8, :right 24, :top 6, :bottom 26 }))

(defmulti take-kinoko :class)
(defmethod take-kinoko :gpin [this]
  (update
   (if (= 0 (this :big))
     (merge this { :id (asset "gpin64"), :width 64, :height 64, :big 1})
     this) :hp + 50))

(defmethod take-kinoko :mukku [this]
  (-> (if (= 0 (this :big))
        (merge this { :id (asset "mukku64"), :width 64, :height 64, :big 1})
        this)
      (update ,, :hp + 50)
      (merge ,, { :goal_x 0 :goal_y 0 })))

(defmethod entity-update :gpin [this gs]
  [
   (case (this :state)
     0 (move-chara this gs)
     2 (let [this1 (update this :explode_count inc)]
         (if (= 100 (this1 :explode_cnt))
           (update this1 :state 1)
           this1))
     this)
   ]
  )

;; class GPinPlayer extends GPin {
;;   takeKinoko() {
;;     if (!this.big) {
;;       this.id = asset("gpin64_p");
;;       this.width = this.height = 64;
;;       this.big = 1;
;;     }
;;     this.hp += 50;
;;   }

;;   update(game_state) {
;;     if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 100)
;;         this.state = 1;
;;     }
;;   }
;; }

;; class Mukku extends Character {
;;   draw(ctx, frame) {
;;     if (this.state == 2) {
;;       var [x, y] = this.getCenter();
;;       ctx.drawImage(asset("mukku_explode"), x - 32, y - 32);
;;     } else {
;;       Character.prototype.draw.apply(this, [ctx, frame]);
;;     }
;;   }

;;   hitBox() {
;;     if (this.big) {
;;       return { left: 16, right: 48, top: 12, bottom: 52 };
;;     } else {
;;       return { left: 8, right: 24, top: 6, bottom: 26 };
;;     }
;;   }

(defmulti take-kinoko :class)
;;   takeKinoko() {
;;     if (this.big == 0) {
;;       this.id = asset("mukku64");
;;       this.width = this.height = 64;
;;       this.big = 1;
;;     }
;;     this.hp += 50;
;;     this.goal_x = this.goal_y = 0;
;;   }
(defmethod take-kinoko :mukku [this]
  (-> (if (= 0 (this :big))
        (merge this { :id (asset "mukku64"), :width 64, :height 64, :big 1 })
        this)
      (update :hp + 50)
      (merge { :goal_x 0 :goal_y 0})))

;;   update(game_state) {
;;     if (this.state == 0) {
;;       if (this.move_type == 100)
;;         moveToKinokoKusa(this, game_state);
;;       else if (this.move_type == 2)
;;         moveType2(this, game_state);
;;       else if (this.move_type == 3)
;;         moveToPlayer(this, game_state);
;;       else
;;         moveChara(this, game_state);

;;       if (game_state.frame % (random(800) + 700) == 0)
;;         game_state.addChara(
;;           new Unk({id: asset("unk"),
;;                    x: this.x + 8, y: this.y + 8, state: 2,
;;                    hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));

;;       if (game_state.frame % (random(700) + 300) == 0) {
;;         var angle = Math.atan2(game_state.gpin_p.y - this.y, game_state.gpin_p.x - this.x);
;;         game_state.addChara(
;;           new Tama({id: asset("tama"),
;;                     x: this.x + 8, y: this.y + 8,
;;                     dx: 2*Math.cos(angle), dy: 2*Math.sin(angle),
;;                     hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));
;;       }
;;     } else if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 30)
;;         this.state = 1;
;;     }
;;   }

(defmethod entity-update :mukku [mukku gs]
  (cond
    (zero? (mukku :state))
    (let [mukku (case (mukku :move_type)
                   100 (move-to-kinoko-kusa mukku gs)
                   2 (move-type-2 mukku gs)
                   3 (move-to-player mukku gs)
                   (move-chara mukku))]

      (concat (if (zero? (mod $frame (+ (random 800) 700)))
                [(make-entity
                  { :class :unk
                   :id (asset "unk")
                   :x (+ 8 (mukku :x))
                   :y (+ 8 (mukku :y))
                   :state 2
                   :hit_box { :left 3, :right 13, :top 3, :bottom 13}})]
                 [])
              (if (zero? (mod $frame (+ (random 700) 300)))
                (let [angle (Math/atan2 (- ((gs :gpin_p) :y) (mukku :y))
                                        (- ((gs :gpin_p) :x) (mukku :x)))]
                  [(make-entity { :class :tama
                                 :id (asset "tama")
                                 :x (+ 8 (mukku :x))
                                 :y (+ 8 (mukku :y))
                                 :dx (* 2 (Math/cos angle))
                                 :dy (* 2 (Math/sin angle))
                                 :hit_box { :left 3 :right 13 :top 3 :bottom 13 }})])
                [])
              [mukku]))

    (= 2 (mukku :state))
    (let [mukku (update mukku :explode_cnt inc)]
      [(if (= 30 (mukku :explode_cnt))
         (update mukku :state (constantly 1))
         mukku)])
    :else
    [mukku]))


(defn out-of-field [entity]
  (or (< (+ (entity :x) (entity :width)) 0)
      (< (+ (entity :y) (entity :height)) 0)
      (> (entity :x) $field_w)
      (> (entity :y) $field_h)))

(defmethod entity-update :tama [this gs]
  (let [tama (if (= 0 (this :state))
               (let [this1 (-> this
                              (update ,, :x + (this :dx))
                              (update ,, :y + (this :dy)))]
                 (if (out-of-field this1)
                   (update this1 :state (constantly 1))
                   this1))
               this)]
    [tama]))

;;   takeKusa(game_state) {
;;     this.hp += 10;
;;     this.goal_x = this.goal_y = 0;
;;     game_state.addChara(new Mukku({id: asset("mukku32"),
;;                                   x: this.x, y: this.y,
;;                                   hp: 40, move_type: random(10),
;;                                   dir: random(4), dir_time: 100 + random(80)}));
;;   }
;; }

;; class Tama extends Entity {
;;   update(game_state) {
;;     if (this.state == 0) {
;;       this.x += this.dx;
;;       this.y += this.dy;

;;       if (this.x + this.width < 0 ||
;;           this.y + this.height < 0||
;;           this.x > $field_w ||
;;           this.y > $field_h)
;;         this.state = 1;
;;     }
;;   }
;; }

;; class Unk extends Entity {
;;   update(game_state) {
;;     if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 30)
;;         this.state = 0;
;;     }
;;   }
;; }

;; class Kinoko extends Entity {}
;; class Kusa extends Entity {}

;; class GameState {
;;   constructor(params = {}) {
;;     this.reset(params);
;;   }

(defn make-game-state []
  (reset {}))

(defn gc-charas [list]
  (remove #(= (% :state) 1)
          list))

(defn search-by-class [gs class]
  (filter #(= class (% :class)) (all gs)))

(defn create-gpin-p []
  (make-entity {:class :gpin
                :id (asset "gpin32_p")
                :x (random $field_w)
                :y (random $field_h)
                :hp 50}))

(defn create-first-mukku []
  (make-entity {:class :mukku,
                :id (asset "mukku32"),
                :x (random $field_w),
                :y (random $field_h),
                :hp 50,
                :move_type 100,
                :dir (random 4),
                :dir_time (+ 100 (random 80))}))

(defn reset [gs]
  (let [gs1 (merge {:gpin_p (create-gpin-p) :list '()} gs)]
    (add-chara gs1 (create-first-mukku))))

(defn add-chara [gs chara]
  (update gs :list #(cons chara %)))

(defn all [gs]
  (cons (gs :gpin_p) (gs :list)))

(defn maplist [f ls]
  (map f
       (take (count ls) (iterate rest ls))))

(defn combinations [items n]
  (cond
    (< (count items) n) '()
    (zero? n) '(())

    :else
    (apply concat
           (maplist (fn [sublis]
                      (map (fn [tail]
                             (cons (first sublis) tail))
                           (combinations (rest sublis) (- n 1))))
                    items))))

;;   handleCollision() {
;;     var all = this.all();
;;     for (var i = 0; i < all.length - 1; i++) {
;;       for (var j = i + 1; j < all.length; j++) {
;;         var one = all[i], other = all[j];

;;         while (one.collide(other)) {
;;           if (one instanceof Character) {
;;             if (other instanceof Character) {
;;               this.charaCharaHit(one, other);
;;             } else if (other instanceof Kusa) {
;;               this.charaHitKusa(one, other);
;;             } else if (other instanceof Kinoko) {
;;               this.charaHitKinoko(one, other);
;;             } else if (other instanceof Unk) {
;;               this.charaUnkHit(one, other);
;;             } else if (other instanceof Tama) {
;;               this.charaTamaHit(one, other);
;;             }
;;           } else if (other instanceof Character) {
;;             [one, other] = [other, one]
;;             continue;
;;           }
;;           break;
;;         }
;;       }
;;     }
;;   }

(defn handle-collision [gs]
  (let [arr (apply vector (all gs))
        pairs (combinations (take (count arr) (iterate inc 0)) 2)
        [arr1 objs] (reduce
                     (fn [[arr obj-acc] [i j]]
                       (if (collide (arr i) (arr j))
                         (let [[one other new-objs] (hit (arr i) (arr j))]
                           (assert one)
                           (assert other)
                           (assert (seq? new-objs))
                           [
                            (-> arr
                                (update i (constantly one))
                                (update j (constantly other)))
                            (concat new-objs obj-acc)
                            ])
                         [arr obj-acc]))
                     [arr ()]
                     pairs)]
    (let [all-objs (apply list (concat arr1 objs))]
      (-> gs
          (update :gpin_p (constantly (first all-objs)))
          (update :list (constantly (rest all-objs)))))))

;;           if (one instanceof Character) {
;;             if (other instanceof Character) {
;;               this.charaCharaHit(one, other);
;;             } else if (other instanceof Kusa) {
;;               this.charaHitKusa(one, other);
;;             } else if (other instanceof Kinoko) {
;;               this.charaHitKinoko(one, other);
;;             } else if (other instanceof Unk) {
;;               this.charaUnkHit(one, other);
;;             } else if (other instanceof Tama) {
;;               this.charaTamaHit(one, other);
;;             }
;;           } else if (other instanceof Character) {
;;             [one, other] = [other, one]
;;;; -> one other new-objs

(defn character? [obj]
  (or (= (obj :class) :mukku) (= (obj :class) :gpin)))

(defn hit [one other]
  (if (character? one)
    (cond
      (or (= (other :class) :mukku) (= (other :class) :gpin)) (chara-chara-hit one other)

      (= (other :class) :kusa)
      (chara-hit-kusa one other)

      (= (other :class) :kinoko)
      (chara-hit-kinoko one other)

      (= (other :class) :unk)
      (chara-unk-hit one other)

      (= (other :class) :tama)
      (chara-tama-hit one other))
    (if (or (= (other :class) :mukku) (= (other :class) :gpin))
      (hit other one)
      [one other ()] ;; nothing happens
      )))

;;;;;;;(defn handle-collision [gs]

;;   update(keystate) {
;;     if (this.defeat() || this.victory())
;;       $scene = "result";

;;     movePlayer(this.gpin_p, keystate);
;;     for (var obj of this.all())
;;       obj.update(this);
;;     if (this.frame % 120 == 0) {
;;       this.addChara(
;;         new Kusa({id: asset("kusa"),
;;                   x: random($field_w - 16), y: random($field_h - 16),
;;                   hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));
;;     }
;;     if (this.frame % 1720 == 0) {
;;       this.addChara(
;;         new Kinoko({id: asset("kinoko"),
;;                     x: random($field_w - 16), y: random($field_h - 16),
;;                     hit_box: { left: 3, right: 13, top: 3, bottom: 13}}))
;;     }
;;     this.handleCollision();
;;     for (var chara of this.searchByClass(Character))
;;       chara.checkHp();
;;     this.gcCharas();
;;     this.frame++;

;;     clearDisplay("black");
;;     for (var obj of this.frame%2==0 ? this.all() : this.all().reverse())
;;       obj.draw($ctx, this.frame);
;;     $ctx.drawImage(asset("gpin32_p"), 650, 190);
;;     drawStringSolid(`H P  ${this.gpin_p.hp}`, 690, 200,
;;                     { color: "white", font: "16px sans-serif" });
;;     $ctx.fillStyle = "white";
;;     $ctx.fillRect(640, 0, 1, 480);
;;   }

(defmethod entity-update :default [obj gs]
  [obj])

(defn make-kinoko []
  (make-entity { :class :kinoko
                :id (asset "kinoko")
                :x (random (- $field_w 16))
                :y (random (- $field_h 16))
                :hit_box { :left 3, :right 13, :top 3, :bottom 13 }}))

(defn add-kinoko [list]
  (if (zero? (mod $frame 1720))
    (cons (make-kinoko) list)
    list))

(defn add-kusa [list]
  (if (zero? (mod $frame 120))
    (cons (make-entity
           { :class :kusa
            :id (asset "kusa")
            :x (random (- $field_w 16))
            :y (random (- $field_h 16))
            :hit_box { :left 3, :right 13, :top 3, :bottom 13 }})
           list)
    list))

(defn spy [v]
  (js/alert (str v))
  v)

(defn draw-game-state [gs]
  (clear-display "black")
  (doseq [obj (all gs)]
    (draw obj))
  (draw-status (gs :gpin_p))

  (draw-string-solid (str "Objects: " (count (:list gs))) 690 250
                     { :color "white" :font "16px sans-serif" })

  (draw-border))

(defn game-state-update [gs]
  (draw-game-state gs)

  (let [gs1 (-> gs
                (merge {:gpin_p (check-hp (first (entity-update (move-player (gs :gpin_p)) gs))),
                        :list (->> (mapcat #(entity-update % gs) (gs :list))
                                   (add-kusa ,,)
                                   (add-kinoko ,,)
                                   (map check-hp ,,)
                                   (gc-charas ,,))
                        })
                (handle-collision))]
    (set! $frame (+ 1 $frame))

    (let [next (if (or (defeat gs1) (victory gs1)) game-over-message game-state-update)]
      (list next gs1))))

(defn draw-status [gpin_p]
  (.drawImage $ctx (asset "gpin32_p") 650 190)
  (draw-string-solid (str "H P  " (gpin_p :hp)) 690 200
                     { :color "white" :font "16px sans-serif" }))

(defn draw-border []
  (set! (.-fillStyle $ctx) "white")
  (.fillRect $ctx 640 0 1 480))

;;   victory() {
;;     return !this.defeat() &&
;;       this.list.filter(obj => obj instanceof Mukku).length == 0;
;;   }

(defn victory [gs]
  (and (not (defeat gs))
       (zero? (count (filter #(= (:class %) :mukku) (gs :list))))))

;;   defeat() {
;;     return this.gpin_p.state != 0;
;;   }

(defn defeat [gs]
  (not (= ((gs :gpin_p) :state) 0)))

;;   charaCharaHit(one, other) {
;;     if (one instanceof GPin && other instanceof GPin ||
;;         one instanceof Mukku && other instanceof Mukku)
;;       return;

;;     var tmp = one.hp;
;;     one.hp -= other.hp;
;;     other.hp -= tmp;
;;   }

(defn chara-chara-hit [one other]
  (if (= (one :class) (other :class))
    (list one other ())
    (list (update one :hp - (other :hp))
          (update other :hp - (one :hp))
          ())))

;;   charaUnkHit(chara, unk) {
;;     if (chara instanceof Mukku) return;

;;     asset("hit_unk").play();
;;     unk.state = 1;
;;     chara.hp -= 5;
;;   }

(defn chara-unk-hit [chara unk]
  (if (= :mukku (chara :class))
    (list chara unk ())
    (do
      (.play (asset "hit_unk"))
      (list (update chara :hp - 5)
            (update unk :state (constantly 1))
            ()))))

;;   charaTamaHit(chara, tama) {
;;     if (chara instanceof Mukku) return;

;;     asset("hit_unk").play();
;;     tama.state = 1;
;;     chara.hp -= 10;
;;   }

(defn chara-tama-hit [chara tama]
  (if (= :mukku (chara :class))
    (list chara tama ())
    (do
      (.play (asset "hit_unk"))
      (list (update chara :hp - 10)
            (update tama :state (constantly 1))
            ()))))

;;   charaHitKusa(chara, kusa) {
;;     asset("hit_kusa").play();
;;     kusa.state = 1;
;;     chara.takeKusa(this)
;;   }

;;   takeKusa(game_state) {
;;     this.hp += 10;
;;     game_state.addChara(new GPin({id: asset("gpin32"), x: this.x, y: this.y,
;;                                   hp: 40, dir: random(4),
;;                                   dir_time: 100 + random(80)}));
;;   }
;; }

(defmulti take-kusa :class)
(defmethod take-kusa :gpin [chara]
  [(update chara :hp + 10)
   (list (make-entity {:id (asset "gpin32"), :class :gpin :x (chara :x), :y (chara :y),
                       :hp 40, :dir (random 4),
                       :dir_time (+ 100 (random 80))}))])

(defmethod take-kusa :mukku [chara]
  [(-> chara
       (merge ,, { :goal_x 0 :goal_y 0})
       (update ,, :hp + 10))
   (list (make-entity {:id (asset "mukku32"), :class :mukku :x (chara :x), :y (chara :y),
                       :hp 40, :move_type (random 10), :dir (random 4),
                       :dir_time (+ 100 (random 80))}))])


(defn chara-hit-kusa [chara kusa]
  (.play (asset "hit_kusa"))
  (let [[chara new-objs] (take-kusa chara)]
    (assert (or true (seq? new-objs)))
    (list chara
          (update kusa :state (constantly 1))
          new-objs)))

;;   charaHitKinoko(chara, kinoko) {
;;     chara.takeKinoko();
;;     kinoko.state = 1;
;;   }
;; }

(defn chara-hit-kinoko [chara kinoko]
  (list (take-kinoko chara)
        (update kinoko :state (constantly 1))
        ()))

(defn draw-string-solid [string x y opts]
  (set! (.-font $ctx) (opts :font))
  (set! (.-fillStyle $ctx) (opts :color))
  (set! (.-textBaseline $ctx) "top")
  (.fillText $ctx string x y))

;; function gameOverMessage(game_state, keystate) {
;;   if (game_state.defeat()) {
;;     drawStringSolid("GAME OVER", 184, 200,
;;                     { color: "white", font: "80px sans-serif" });
;;   } else if (game_state.victory()) {
;;     clearDisplay("black");
;;     drawStringSolid("V I C T O R Y !!",
;;                     130, 64, { color: "white", font: "80px sans-serif" });
;;     drawStringSolid("C O N G R A T U L A T I O N S",
;;                     20, 196, { color: "white", font: "60px sans-serif" });
;;   }

;;   if (keystate.z) {
;;     $scene = "game";
;;     keystate.z = false;
;;     game_state.reset();
;;   }
;; }

;; function gameStartMessage(keystate) {
;;   clearDisplay("black");

;;   drawStringSolid("Gピン vs Mック", 120, 100,
;;                   { color: "white", font: "80px sans-serif" });
;;   drawStringSolid("PRESS Z TO START", 334, 308,
;;                   { color: "white", font: "16px sans-serif" });

;;   if (keystate.z) {
;;     $scene = "game";
;;     keystate.z = false;
;;   }
;; }

(defn game-start-message [keystate]
  (clear-display "black")
  (draw-string-solid "Gピン vs Mック" 120 100
                     { :color "white", :font "80px sans-serif" })
  (draw-string-solid "PRESS Z TO START" 334 308
                     { :color "white", :font "16px sans-serif" })

  (if (keystate :z)
    (set! $scene "game")
    (update keystate :z #(identity false))))

(defn asset [id]
  (.getElementById js/document id))

(defn fix-chara-position [chara]
  (reduce (fn [acc [field op limit new-dir]]
            (if (op (acc field) limit)
              (update (update acc field (constantly limit))
                      :dir (constantly new-dir))
              acc))
          chara
          [[:x < 0 1]
           [:y < 0 0]
           [:x > (- $field_w (chara :width)) 2]
           [:y > (- $field_h (chara :height)) 3]]))

(defn move-player [player]
  (if (zero? (player :state))
    (cond
      ($keystate :right) (update player :x + (player :dx))
      ($keystate :left) (update player :x - (player :dx))
      ($keystate :up) (update player :y - (player :dy))
      ($keystate :down) (update player :y + (player :dy))
      :else player)
    player))

(defn manhattan [x1 y1 x2 y2]
  (+ (Math/abs (- x1 x2)) (Math/abs (- y1 y2))))

(defn move-chara [chara gs]
  (let [chara
        (if (zero? (mod (chara :update_frame) (chara :dir_time)))
          (update chara :dir (constantly (random 4)))
          chara)]
    (let [chara
          (case (chara :dir)
            0 (update chara :y + (chara :dy))
            1 (update chara :x + (chara :dx))
            2 (update chara :x - (chara :dx))
            3 (update chara :y - (chara :dy)))]
      (let [chara (fix-chara-position chara)]
        (update chara :update_frame + 1)))))

(defn min-by [f coll]
  (if (empty? coll)
    nil
    (apply min-key f coll)))

(defn move-to-kinoko-kusa [chara gs]
  (letfn [(dist [k]
            (manhattan (chara :x) (chara :y) (k :x) (k :y)))]
    (let [target (or (min-by dist (filter #(= (% :class) :kinoko) (gs :list)))
                     (min-by dist (filter #(= (% :class) :kusa) (gs :list)))
                     { :x 0 :y 0 })]
      (if (= 1 (chara :big))
        (move-to chara (- (target :x) 22) (target :y))
        (move-to chara (target :x) (target :y))))))

;; function moveType2(chara, game_state) {
;;   if (chara.hp <= 20) {
;;     var kusa = game_state.searchByClass(Kusa)[0];
;;     if (kusa) {
;;       if (chara.goal_x == 0        &&  chara.goal_y == 0 ||
;;           chara.goal_x == chara.x  &&  chara.goal_y == chara.y) {
;;         chara.goal_x = kusa.x;
;;         chara.goal_y = kusa.y;
;;       }
;;       chara.moveTo(chara.goal_x, chara.goal_y);
;;     } else {
;;       moveChara(chara, game_state);
;;     }
;;   } else {
;;     var gpin1 = game_state.all().filter(e => e instanceof GPin &&
;;                                         !(e instanceof GPinPlayer))[0];
;;     if (gpin1)
;;       chara.moveTo(gpin1.x, gpin1.y);
;;     else
;;       moveChara(chara, game_state);
;;   }
;; }

(defn move-type-2 [chara gs]
  (move-chara chara gs))

(defn move-to-player [mukku gs]
  (let [gpin_p (gs :gpin_p)]
    (fix-chara-position (move-to mukku (gpin_p :x) (gpin_p :y)))))

(defn clear-display [color]
  (set! (.-fillStyle $ctx) color)
  (.fillRect $ctx 0 0 840 480))

(defn set-key [ev, v]
  (when ($browser2game (.-key ev))
        (.preventDefault ev)
        (set! $keystate (merge $keystate {($browser2game (.-key ev)) v}))))

(defn gpin []
  (set! $ctx (.getContext (.getElementById js/document "screen") "2d"))
  (set! $frame 0)
  (set! $scene game-state-update)

  (set! $game-state (make-game-state))

  (set! (.-onkeydown js/document) (fn [ev] (set-key ev true)))
  (set! (.-onkeyup js/document) (fn [ev] (set-key ev false)))

  (letfn [(render [_timestamp]
            (let [[next-scene gs1] ($scene $game-state)]
              (set! $scene next-scene)
              (set! $game-state gs1))
            (.requestAnimationFrame js/window render))]
      (.requestAnimationFrame js/window render)))

;(gpin)
