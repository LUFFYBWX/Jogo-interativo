package com.livecraft

import android.content.Context

enum class Fighter { LUFFY, NARUTO }
data class GiftImpact(val fighter: Fighter, val amount: Int)
data class BattleSnapshot(val luffy: Int, val naruto: Int, val wall: Float, val winner: Fighter?, val impact: GiftImpact?)

object GameState {
 private const val PREFS="anime_push_battle"
 private data class GiftRule(val aliases:Set<String>,val coins:Long,val points:Int,val fighter:Fighter)
 // A identificação combina nome e moedas: assim as duas rosas não se confundem.
 private val rules=listOf(
  GiftRule(setOf("rose","rosa"),1,1,Fighter.LUFFY),
  GiftRule(setOf("mini dino","mini dinosaur","pequeno dinossauro"),10,10,Fighter.LUFFY),
  GiftRule(setOf("paper crane","grou de papel","guindaste de papel"),99,99,Fighter.LUFFY),
  GiftRule(setOf("galaxy","galáxia","galaxia"),1000,1000,Fighter.LUFFY),
  GiftRule(setOf("white rose","rosa branca"),1,1,Fighter.NARUTO),
  GiftRule(setOf("rose","rosa"),10,10,Fighter.NARUTO),
  GiftRule(setOf("hat and mustache","chapéu e bigode","chapeu e bigode"),99,99,Fighter.NARUTO),
  GiftRule(setOf("dinosaur","dinossauro"),1000,1000,Fighter.NARUTO)
 )
 private var prefs:android.content.SharedPreferences?=null
 private var luffy=0;private var naruto=0;private var impact:GiftImpact?=null;private var winner:Fighter?=null
 @Volatile var isRunning=false;private set
 @Volatile var onChanged:(()->Unit)?=null
 fun initialize(context:Context){if(prefs==null)prefs=context.applicationContext.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
 fun saveUsername(username:String){prefs?.edit()?.putString("username",username.trim().removePrefix("@"))?.apply()}
 fun loadUsername()=prefs?.getString("username","").orEmpty()
 @Synchronized fun startGame(){luffy=0;naruto=0;impact=null;winner=null;isRunning=true;onChanged?.invoke()}
 @Synchronized fun stopGame(){isRunning=false;onChanged?.invoke()}
 @Synchronized fun snapshot()=BattleSnapshot(luffy,naruto,(luffy-naruto).toFloat()/1000f,winner,impact)
 @Synchronized fun addGift(giftName:String,giftCount:Int,streakOver:Boolean,coins:Long=0L):Int{
  if(!isRunning||!streakOver||winner!=null)return 0
  val normalized=normalize(giftName)
  val rule=rules.firstOrNull{normalizeSet(it.aliases).contains(normalized)&&it.coins==coins}?:return 0
  val amount=rule.points*giftCount.coerceAtLeast(1)
  if(rule.fighter==Fighter.LUFFY){luffy+=amount;naruto=(naruto-amount).coerceAtLeast(0)}else{naruto+=amount;luffy=(luffy-amount).coerceAtLeast(0)}
  impact=GiftImpact(rule.fighter,amount)
  if(luffy>=1000)winner=Fighter.LUFFY
  if(naruto>=1000)winner=Fighter.NARUTO
  onChanged?.invoke();return if(winner==rule.fighter)1 else 0
 }
 private fun normalizeSet(items:Set<String>)=items.map{normalize(it)}
 private fun normalize(s:String)=java.text.Normalizer.normalize(s.lowercase(),java.text.Normalizer.Form.NFD).replace("\\p{Mn}+".toRegex(),"").filter{it.isLetterOrDigit()}
}