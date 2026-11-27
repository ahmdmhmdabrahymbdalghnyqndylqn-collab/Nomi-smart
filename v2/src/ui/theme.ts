export type Variant='green'|'purple';
export type Mode='light'|'dark';
export const palette=(variant:Variant,mode:Mode)=>{
 const dark=mode==='dark',purple=variant==='purple';
 return {background:dark?(purple?'#15101D':'#101718'):(purple?'#E8E5EF':'#DCE9EA'),surface:dark?(purple?'#241D30':'#182525'):'rgba(247,252,253,0.56)',text:dark?'#F2F9F8':'#101E24',muted:dark?'#B7C9CE':'#405762',primary:dark?(purple?'#C4A2FF':'#66F5A2'):(purple?'#65409D':'#086A38'),accent:purple?'#AA82EF':'#28D86D',border:dark?(purple?'#504263':'#36534C'):'rgba(255,255,255,0.85)',tint:dark?(purple?'#332641':'#233C34'):(purple?'#E2D5F4':'#CBEBDB'),top:dark?(purple?'#20172B':'#14221D'):(purple?'#D9C9ED':'#B4E0C9'),bottom:dark?(purple?'#0E0A16':'#0A1412'):(purple?'#D5C9E7':'#C3DEDA'),shine:purple?'#E4CBFF':'#B9FFD0',deep:purple?'#6735B3':'#08783A',row:dark?'rgba(110,150,145,0.09)':'rgba(223,235,237,0.64)',buttonStart:purple?'#7550B8':'#10783D',buttonEnd:purple?'#532A93':'#075B2E',onPrimary:'#FFFFFF'};
};
