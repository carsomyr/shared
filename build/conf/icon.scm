(let *
  ((aggregate_image (car (gimp-image-new 48 48 RGB)))
   (icon_sizes '(48 32 16)))

  (map
   (lambda (i)
     (let*
         ((infile (string-append "icon_" (string-append (number->string i) ".png")))
          (image (car (file-png-load RUN-NONINTERACTIVE infile infile)))
          (layer ())
          (aggregate_layer ()))

       ;; Create an icon stack member.
       (set! layer (car (gimp-image-get-active-layer image)))
       (set! aggregate_layer (car (gimp-layer-new-from-drawable layer aggregate_image)))
       (gimp-image-add-layer aggregate_image aggregate_layer -1)

       ;; GC the image.
       (gimp-image-delete image)
       )
     )
   icon_sizes
   )

  (file-ico-save RUN-NONINTERACTIVE aggregate_image
                 (car (gimp-image-get-active-layer aggregate_image))
                 "icon.ico" "icon.ico")

  ;; GC the image.
  (gimp-image-delete aggregate_image)
  )
(gimp-quit 0)
