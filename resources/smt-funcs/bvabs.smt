(define-fun bvabs ((x (_ BitVec 32)) (y (_ BitVec 32))) (_ BitVec 32)
    (if (bvsge (bvsub x y) #x00000000) (bvsub x y) (bvsub y x)))
