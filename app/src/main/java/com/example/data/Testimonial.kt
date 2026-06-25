package com.example.data

data class Testimonial(
    val author: String,
    val role: String,
    val comment: String,
    val rating: Float,
    val avatarColorIndex: Int
)

object TestimonialProvider {
    val testimonials = listOf(
        Testimonial(
            author = "Sarah Jenkins",
            role = "Verified Fashion Buyer",
            comment = "I was absolutely blown away by the tailoring of the Woolen Trench Coat! Fits perfectly, keeps me incredibly warm, and looks so luxurious. Delivery was fast too!",
            rating = 5.0f,
            avatarColorIndex = 0
        ),
        Testimonial(
            author = "Marcus Vance",
            role = "Audiophile & Tech Critic",
            comment = "The AeroSound headphones rival any top-tier studio headsets I have tested. Extremely rich bass response and the active noise cancelling works like absolute magic.",
            rating = 4.8f,
            avatarColorIndex = 1
        ),
        Testimonial(
            author = "Elena Rostova",
            role = "Skincare Specialist",
            comment = "The botanical renewal face serum has completely changed my skin texture. It looks glowing, radiant, and hydrated all day. Natural ingredients make a huge difference!",
            rating = 5.0f,
            avatarColorIndex = 2
        ),
        Testimonial(
            author = "Michael S.",
            role = "Executive Director",
            comment = "The Grand Chronograph Watch is a stunning masterwork. Heavy weight, perfect craftsmanship, and a brilliant sapphire glass. I get compliments every single day.",
            rating = 5.0f,
            avatarColorIndex = 3
        )
    )
}
