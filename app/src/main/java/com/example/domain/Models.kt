package com.example.domain

enum class ToolCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val requiresFirebaseAi: Boolean
) {
    AI_EDITING(
        id = "ai_editing",
        title = "AI Photo Editing",
        subtitle = "10 intelligent photo restoration & enhancement tools",
        requiresFirebaseAi = true
    ),
    AI_GENERATOR(
        id = "ai_generator",
        title = "AI Photo Generator",
        subtitle = "10 generative portrait, avatar & artwork studios",
        requiresFirebaseAi = true
    ),
    CREATIVE_STUDIO(
        id = "creative_studio",
        title = "Creative Photo Studio",
        subtitle = "10 canvas template designers with live export",
        requiresFirebaseAi = false
    ),
    ADVANCED_EDITING(
        id = "advanced_editing",
        title = "Advanced Image Tools",
        subtitle = "10 pixel-accurate editing & format conversion tools",
        requiresFirebaseAi = false
    )
}

enum class StudioToolId(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val defaultCredits: Int
) {
    // 1–10: AI Photo Editing Tools
    AI_ENHANCER("ai_enhancer", 1, "AI Photo Enhancer", "Boost clarity, lighting balance, and micro-detail.", ToolCategory.AI_EDITING, 2),
    AI_UPSCALER("ai_upscaler", 2, "AI Image Upscaler", "Upscale portraits and shots to crisp HD / 4K.", ToolCategory.AI_EDITING, 3),
    AI_BG_REMOVER("ai_bg_remover", 3, "AI Background Remover", "Isolate subjects cleanly with transparent cutout.", ToolCategory.AI_EDITING, 2),
    AI_BG_CHANGER("ai_bg_changer", 4, "AI Background Changer", "Replace studio backdrops with luxury scenes.", ToolCategory.AI_EDITING, 3),
    AI_OBJECT_REMOVER("ai_object_remover", 5, "AI Object Remover", "Erase unwanted distractions seamlessly.", ToolCategory.AI_EDITING, 3),
    AI_RETOUCH("ai_retouch", 6, "AI Photo Retouch", "Smooth skin naturally while preserving texture.", ToolCategory.AI_EDITING, 2),
    AI_FACE_ENHANCER("ai_face_enhancer", 7, "AI Face Enhancer", "Refine facial lighting, eyes, and expression.", ToolCategory.AI_EDITING, 2),
    AI_BLUR_BG("ai_blur_bg", 8, "AI Blur Background", "Create creamy f/1.4 DSLR bokeh depth.", ToolCategory.AI_EDITING, 2),
    AI_RESTORATION("ai_restoration", 9, "AI Photo Restoration", "Repair creases, noise, and faded vintage prints.", ToolCategory.AI_EDITING, 3),
    AI_COLOR_CORRECTION("ai_color_correction", 10, "AI Color Correction", "Balance exposure, white point, and cinema tone.", ToolCategory.AI_EDITING, 2),

    // 11–20: AI Photo Generator
    GEN_TEXT_TO_IMAGE("gen_text_to_image", 11, "Text to Image Generator", "Synthesize original imagery from rich prompts.", ToolCategory.AI_GENERATOR, 4),
    GEN_AVATAR("gen_avatar", 12, "AI Avatar Generator", "Craft stylized luxury profile avatars.", ToolCategory.AI_GENERATOR, 4),
    GEN_CARTOON("gen_cartoon", 13, "AI Cartoon Generator", "Transform concepts into bold vector cartoons.", ToolCategory.AI_GENERATOR, 3),
    GEN_ANIME("gen_anime", 14, "AI Anime Generator", "Render cel-shaded studio anime illustrations.", ToolCategory.AI_GENERATOR, 3),
    GEN_3D_CHARACTER("gen_3d_character", 15, "AI 3D Character Generator", "Generate sculpted 3D character renders.", ToolCategory.AI_GENERATOR, 4),
    GEN_PORTRAIT("gen_portrait", 16, "AI Portrait Generator", "Editorial studio portraits with rim lighting.", ToolCategory.AI_GENERATOR, 4),
    GEN_COUPLE("gen_couple", 17, "AI Couple Photo Generator", "Romantic editorial dual-subject compositions.", ToolCategory.AI_GENERATOR, 4),
    GEN_BABY("gen_baby", 18, "AI Baby Photo Generator", "Warm milestone keepsake studio portraits.", ToolCategory.AI_GENERATOR, 4),
    GEN_HEADSHOT("gen_headshot", 19, "AI Professional Headshot", "Executive corporate & LinkedIn studio shots.", ToolCategory.AI_GENERATOR, 4),
    GEN_FASHION("gen_fashion", 20, "AI Fashion Photo Generator", "High-fashion lookbook and runway photography.", ToolCategory.AI_GENERATOR, 4),

    // 21–30: Creative Photo Studio (Real Canvas + Export)
    STUDIO_IG_DP("studio_ig_dp", 21, "Instagram DP Maker", "Circular avatar framing with gold studio rings.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_YT_THUMB("studio_yt_thumb", 22, "YouTube Thumbnail Maker", "16:9 high-impact headline & badge canvas.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_IG_POST("studio_ig_post", 23, "Instagram Post Maker", "1:1 editorial social card with title bar.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_FESTIVAL("studio_festival", 24, "Festival Poster Maker", "Ornate celebration poster with custom text.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_WEDDING("studio_wedding", 25, "Wedding Photo Editor", "Ivory & gold monogram frame for couples.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_BIRTHDAY("studio_birthday", 26, "Birthday Poster Maker", "Golden celebration card with custom wishes.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_LOGO("studio_logo", 27, "Logo Maker", "Monogram crest & luxury brand emblem studio.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_COLLAGE("studio_collage", 28, "Photo Collage Maker", "Stitch up to 4 photos into a gold-lined grid.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_FRAME("studio_frame", 29, "Photo Frame Editor", "Gallery mat & brushed metallic frame studio.", ToolCategory.CREATIVE_STUDIO, 0),
    STUDIO_PASSPORT("studio_passport", 30, "Passport Size Photo Maker", "6-up 35x45mm print sheet with cut guides.", ToolCategory.CREATIVE_STUDIO, 0),

    // 31–40: Advanced Image Editing Tools (Real Bitmap Engine)
    ADV_CROPPER("adv_cropper", 31, "Image Cropper", "Precision aspect ratio cropping (1:1, 4:5, 16:9, 3:4).", ToolCategory.ADVANCED_EDITING, 0),
    ADV_RESIZER("adv_resizer", 32, "Image Resizer", "Exact pixel dimension scaling & presets.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_COMPRESSOR("adv_compressor", 33, "Image Compressor", "Reduce KB size with live JPEG/WEBP compression.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_FILTERS("adv_filters", 34, "Image Filters", "ColorMatrix studio presets (Noir, Golden, Vivid).", ToolCategory.ADVANCED_EDITING, 0),
    ADV_EFFECTS("adv_effects", 35, "Photo Effects", "Vignette, contrast, warmth, and gold duotone.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_TEXT("adv_text", 36, "Add Text to Image", "Custom Montserrat typography overlays.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_STICKERS("adv_stickers", 37, "Add Stickers", "Luxury badges, crowns, sparkles, and emblems.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_BORDER("adv_border", 38, "Photo Border Maker", "Adjustable borders in Gold, Obsidian & Ivory.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_CONVERTER("adv_converter", 39, "Image Format Converter", "Convert seamlessly between PNG, JPG, and WEBP.", ToolCategory.ADVANCED_EDITING, 0),
    ADV_WATERMARK("adv_watermark", 40, "Photo Watermark Tool", "Corner or tiled diagonal copyright watermark.", ToolCategory.ADVANCED_EDITING, 0);

    companion object {
        fun byId(id: String): StudioToolId? = entries.firstOrNull { it.id == id }
        fun byCategory(category: ToolCategory): List<StudioToolId> = entries.filter { it.category == category }
    }
}

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    PNG("PNG", "png", "image/png"),
    JPEG("JPG", "jpg", "image/jpeg"),
    WEBP("WEBP", "webp", "image/webp")
}
