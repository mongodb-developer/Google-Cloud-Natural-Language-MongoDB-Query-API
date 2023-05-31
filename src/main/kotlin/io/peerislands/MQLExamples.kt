package io.peerislands

val updateKeywords = listOf("update", "change", "modify", "alter")
val insertKeywords = listOf("insert", "add", "create")
val deleteKeywords = listOf("delete", "remove", "erase")
val findKeywords = listOf("find", "search", "locate", "get")

val updateExamples = """
        db.inventory.updateOne(
           { item: 'paper' },
           {
             ${'$'}set: { 'size.uom': 'cm', status: 'P' },
             ${'$'}currentDate: { lastModified: true }
           }
        )
        
        db.inventory.updateMany(
           { 'qty': { ${'$'}lt: 50 } },
           {
             ${'$'}set: { 'size.uom': 'in', status: 'P' },
             ${'$'}currentDate: { lastModified: true }
           }
        )
    """.trimIndent()

val deleteExamples = """
        db.inventory.deleteOne({ status : 'D' })
        
        db.inventory.deleteMany({ status : 'A' })
""".trimIndent()

val findExamples = """
        db.inventory.find( { status: 'D' } )
        
        db.inventory.find( { status: 'A' } )
""".trimIndent()

val insertExamples = """
        db.inventory.insertOne(
           { item: 'canvas',
             qty: 100,
             tags: ['cotton'],
             size: { h: 28, w: 35.5, uom: 'cm' }
           }
        )
        
        db.inventory.insertMany([
           { item: 'journal',
             qty: 25,
             tags: ['blank', 'red'],
             size: { h: 14, w: 21, uom: 'cm' }
           },
           { item: 'mat',
             qty: 85,
             tags: ['gray'],
             size: { h: 27.9, w: 35.5, uom: 'cm' }
           },
           { item: 'mousepad',
             qty: 25,
             tags: ['gel', 'blue'],
             size: { h: 19, w: 22.85, uom: 'cm' }
           }
        ])
""".trimIndent()