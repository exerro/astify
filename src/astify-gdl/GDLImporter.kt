import astify.TextPosition
import glh.util.SingleSourceImporter

typealias GDLSourceID = Unit

internal class GDLImporter(
        gdl: GDL
): SingleSourceImporter<GDL, Pair<String, TextPosition>>(gdl)
