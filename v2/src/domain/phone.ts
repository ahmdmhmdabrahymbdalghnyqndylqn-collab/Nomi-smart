export type Country='EG'|'JO';
export function normalizePhone(raw:string,country:Country):string|null{
 const s=raw.trim().replace(/[٠-٩]/g,d=>String('٠١٢٣٤٥٦٧٨٩'.indexOf(d))).replace(/[۰-۹]/g,d=>String('۰۱۲۳۴۵۶۷۸۹'.indexOf(d))).replace(/[\s()-]/g,'');
 const cc=country==='EG'?'20':'962';
 let n=s;
 if(n.startsWith('+'+cc))n=n.slice(cc.length+1);
 else if(n.startsWith('00'+cc))n=n.slice(cc.length+2);
 else if(n.startsWith(cc))n=n.slice(cc.length);
 if(n.startsWith('0'))n=n.slice(1);
 return (country==='EG'?/^1[0125]\d{8}$/:/^7\d{8}$/).test(n)?'+'+cc+n:null;
}
