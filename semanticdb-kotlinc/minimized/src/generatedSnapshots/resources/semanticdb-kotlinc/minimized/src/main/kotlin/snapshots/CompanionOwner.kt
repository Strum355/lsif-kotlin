package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#
//                   documentation ```kotlin\npublic final class CompanionOwner\n```
//    ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#`<init>`().
//                   documentation ```kotlin\npublic constructor CompanionOwner()\n```
  companion object {
//          ^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#Companion# 1:0
//                   documentation ```kotlin\npublic companion object\n```
    fun create(): CompanionOwner = CompanionOwner()
//      ^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#Companion#create().
//             documentation ```kotlin\npublic final fun create(): snapshots.CompanionOwner\n```
//                ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#
//                                 ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#`<init>`().
  }
  fun create(): Int = CompanionOwner.create().hashCode()
//    ^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#create().
//           documentation ```kotlin\npublic final fun create(): kotlin.Int\n```
//              ^^^ reference semanticdb maven . . kotlin/Int#
//                    ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#Companion#
//                                   ^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#Companion#create().
//                                            ^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#hashCode(+-1).
}
