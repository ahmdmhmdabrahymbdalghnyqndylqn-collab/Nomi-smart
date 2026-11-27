export type Variant='green'|'purple';
export type Mode='light'|'dark';
export const palette=(variant:Variant,mode:Mode)=>{
 const dark=mode==='dark',purple=variant==='purple';
 return {background:dark?(purple?'#1B1524':'#101D17'):(purple?'#F4EEFB':'#EDF8F2'),surface:dark?(purple?'#30263F':'#193428'):'rgba(255,255,255,0.84)',text:dark?'#F5F9F6':purple?'#302348':'#16342A',muted:dark?'#C1D7CD':'#4B675C',primary:dark?(purple?'#D1B2FF':'#8EE5B0'):(purple?'#6940A2':'#137B4C'),accent:purple?'#A478E2':'#26BC70',border:dark?'#51675D':'#FFFFFF',tint:dark?(purple?'#443458':'#254B36'):(purple?'#EADDFA':'#D9F2E4')};
};
