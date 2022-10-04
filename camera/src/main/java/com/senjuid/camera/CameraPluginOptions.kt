package com.senjuid.camera

class CameraPluginOptions private constructor(
        val maxSize: Int?,
        val quality: Int?,
        val name: String?,
        val isFacingBack: Boolean?,
        val disableFacingBack: Boolean?,
        val disableMirroring: Boolean?,
        val snapshot: Boolean?
) {
    data class Builder(
            private var maxSize: Int? = 0,
            private var quality: Int? = 100,
            private var name: String? = "img_lite",
            private var isFacingBack: Boolean? = false,
            private var disableFacingBack: Boolean? = false,
            private var disableMirroring: Boolean? = true,
            private var snapshot: Boolean? = true
    ) {
        fun setMaxSize(maxSize: Int) = apply { this.maxSize = maxSize }
        fun setQuality(quality: Int) = apply { this.quality = quality }
        fun setName(name: String) = apply { this.name = name }
        fun setIsFacingBack(facingBack: Boolean) = apply { this.isFacingBack = facingBack }
        fun setDisableFacingBack(disable: Boolean) = apply { this.disableFacingBack = disable }
        fun setDisableMirroring(disable: Boolean) = apply { this.disableMirroring = disable }
        fun setSnapshot(snapshot: Boolean) = apply { this.snapshot = snapshot }
        fun build() = CameraPluginOptions(maxSize, quality, name, isFacingBack, disableFacingBack, disableMirroring, snapshot)
    }
}