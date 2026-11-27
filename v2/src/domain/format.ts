export type Currency = 'EGP' | 'JOD';
export function currencyFor(country: 'EG' | 'JO'): Currency { return country === 'EG' ? 'EGP' : 'JOD'; }
export function minorScale(currency: Currency): number { return currency === 'EGP' ? 100 : 1000; }
export function parseAmount(value: string, currency: Currency): number {
 const normalized = value.trim().replace(/[٠-٩]/g, d => String('٠١٢٣٤٥٦٧٨٩'.indexOf(d))).replace('٫','.');
 const digits = currency === 'EGP' ? 2 : 3;
 if (!new RegExp('^\\d+(?:\\.\\d{1,'+digits+'})?$').test(normalized)) throw new Error('Invalid amount');
 const [whole, fraction=''] = normalized.split('.');
 const amount = Number(whole)*minorScale(currency)+Number(fraction.padEnd(digits,'0'));
 if (!Number.isSafeInteger(amount) || amount <= 0) throw new Error('Invalid amount');
 return amount;
}
export function balance(entries: {currency: Currency; minor: number; kind:'income'|'expense'}[], currency: Currency): number {
 return entries.filter(e=>e.currency===currency).reduce((sum,e)=>sum+(e.kind==='income'?e.minor:-e.minor),0);
}
export function formatMoney(minor:number,currency:Currency,locale:string):string {
 return new Intl.NumberFormat(locale,{style:'currency',currency}).format(minor/minorScale(currency));
}
export function notificationRoute(data:unknown):string|null {
 if (!data || typeof data!=='object') return null;
 const d=data as Record<string,unknown>;
 if (d.type==='prayer') return '/prayer';
 if (d.type==='alarm') return '/alarms';
 if (d.type==='task' && typeof d.id==='string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(d.id)) return '/tasks';
 return null;
}
