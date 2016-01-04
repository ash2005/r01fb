package r01f.services.shf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Document;

import r01f.ejie.model.shf.SignatureRequestOutputData;
import r01f.ejie.model.shf.SignatureVerifyOutputData;
import r01f.guids.CommonOIDs.AppCode;
import x43f.ejie.com.X43FNSHF.Body;
import x43f.ejie.com.X43FNSHF.EjgvDocument;
import x43f.ejie.com.X43FNSHF.Header;
import x43f.ejie.com.X43FNSHF.VerificationResult;

public class SignatureServiceMockImpl 
  implements SignatureService {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public SignatureServiceForApp requiredBy(final AppCode appCode) {
		return new SignatureServiceForApp() {
			private static final String mockSignature = "PGRzaWc6U2lnbmF0dXJlIHhtbG5zOmRzaWc9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiIElkPSJJZDE" +
														"xMzAxMDEzMDEyMDAyMzY0NjAxOTIxNjcyNTQiPg0KCTxkc2lnOlNpZ25lZEluZm8gSWQ9IklkMjExOTk2Mzk0ODE1Mjk4OD" +
														"kyMzQxNzI2MTEyMjgzIj4NCgkJPGRzaWc6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cud" +
														"zMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPg0KCQk8ZHNpZzpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRw" +
														"Oi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4NCgkJPGRzaWc6UmVmZXJlbmNlIElkPSJJZDczNTI" +
														"2MTQxNzI2MjE4MDg3ODIxNDY4NzE5MyIgVHlwZT0iaHR0cDovL3VyaS5ldHNpLm9yZy8wMTkwMyNTaWduZWRQcm9wZXJ0aW" +
														"VzIiBVUkk9IiNJZDE5NTY4ODU4NTMyMDg0ODU1NzcwODM4NjkxNTU5Ij4NCgkJCTxkc2lnOlRyYW5zZm9ybXM+DQoJCQkJP" +
														"GRzaWc6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+DQoJ" +
														"CQk8L2RzaWc6VHJhbnNmb3Jtcz4NCgkJCTxkc2lnOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3J" +
														"nLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+DQoJCQk8ZHNpZzpEaWdlc3RWYWx1ZT5Wa2Z4YXRVRnduaEpvRjgrcnZqZVJKZn" +
														"pzNTg9PC9kc2lnOkRpZ2VzdFZhbHVlPg0KCQk8L2RzaWc6UmVmZXJlbmNlPg0KCQk8ZHNpZzpSZWZlcmVuY2UgSWQ9IklkM" +
														"TUxNDIzNjg0NTgzMDc1ODQzODc2NTgzMTA5IiBVUkk9InVybjpkZXRhY2hlZCI+DQoJCQk8ZHNpZzpEaWdlc3RNZXRob2Qg" +
														"QWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPg0KCQkJPGRzaWc6RGlnZXN0VmF" +
														"sdWU+TWkzTUpWczE3NE9aSVpILzQzeFhaLzFTZnFBPTwvZHNpZzpEaWdlc3RWYWx1ZT4NCgkJPC9kc2lnOlJlZmVyZW5jZT" +
														"4NCgk8L2RzaWc6U2lnbmVkSW5mbz4NCgk8ZHNpZzpTaWduYXR1cmVWYWx1ZSBJZD0iSWQxMzQ2NDg4ODA5NzE2OTI0NjAxN" +
														"TI2OTMwMzkxIj5PTnhYR3h0VnNkdDlkTlV3cHdUVWdmc2ZlOVp3V24rOWNQbUtwcjFTOWplMHVqbGRVL0M3YXN2cGFiWFFi" +
														"YUdzemRTOGt5VGtQNUhVOFNRQUJ2U0VVeUc4YjJTd0NBVEg3bHN6T0VZdXE0c3ppZEVySnVFd3FMWDdnWU8zQWlhTEMyZXZ" +
														"mZGttWEdFeFcrTUdWb3Q1aHg5alR3TUhYU1k2OVY3dy9uMFdCZG1lQXRjalpmQU52K2hmMnFlTXhIdFZrMERWNmEyK0dPUW" +
														"R1eFJ3TFYvME9LaUc3OU15bmM2cEFUOWFRRXFjR2ZzVHk2b292TTF6Vkd2Q2RCdUdMTDJYNHY0MFI2OHNNbDdiUVRXSElle" +
														"WdMSEhudlc3c0FpNzlsbW9acEowSVFiakt0em9pZEx3N0c3UW5MdFRva3lwazFaNkJOUjdFVzJSb2xvbEhhSGtpcUE9PTwv" + 
														"ZHNpZzpTaWduYXR1cmVWYWx1ZT4NCgk8ZHNpZzpLZXlJbmZvPg0KCQk8ZHNpZzpYNTA5RGF0YT4NCgkJCTxkc2lnOlg1MDl" + 
														"DZXJ0aWZpY2F0ZT5NSUlJenpDQ0JyZWdBd0lCQWdJQ0NOQXdEUVlKS29aSWh2Y05BUUVGQlFBd2daVXhDekFKQmdOVkJBWV" + 
														"RBa1ZUTVJRd0VnWURWUVFLREF0SldrVk9VRVVnVXk1QkxqRTZNRGdHQTFVRUN3d3hRVnBhSUZwcGRYSjBZV2RwY21rZ2NIV" + 
														"mliR2xyYjJFZ0xTQkRaWEowYVdacFkyRmtieUJ3ZFdKc2FXTnZJRk5EUVRFME1ESUdBMVVFQXd3clEwRWdjR1Z5YzI5dVlX" +
														"d2daR1VnUVVGUVVDQjJZWE5qWVhNZ0tESXBJQzBnUkVWVFFWSlNUMHhNVHpBZUZ3MHhNakV3TWpReE5ETXdNakphRncweE5" + 
														"URXdNalF4TkRNd01qSmFNSUhnTVFzd0NRWURWUVFHRXdKRlV6RXJNQ2tHQTFVRUNnd2lSVlZUUzA4Z1NrRlZVa3hCVWtsVV" + 
														"drRWdMU0JIVDBKSlJWSk9UeUJXUVZORFR6RTBNRElHQTFVRUN3d3JXa2xWVWxSQlIwbFNTU0JQVGtGU1ZGVkJJQzBnUTBWU" + 
														"1ZFbEdTVU5CUkU4Z1VrVkRUMDVQUTBsRVR6RWJNQmtHQTFVRUN3d1NjMlZzYkc4Z1pXeGxZM1J5dzdOdWFXTnZNUkl3RUFZ" + 
														"RFZRUUZFd2xUTkRnek16QXdNVU14UFRBN0JnTlZCQU1NTkVSSlVrVkRRMGxQVGlCRVJTQkpUazVQVmtGRFNVOU9JRmtnUVV" + 
														"STlNVNUpVMVJTUVVOSlQwNGdSVXhGUTFSU1QwNUpRMEV3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW" + 
														"9JQkFRQ0taVUZhQ1ptQVhibFRSMkxnVTVZdWFVN0FsYWRKNjMwWmNaZHYwYllTK3pjb3UxT24zRnBTdEttcS92bC82N2Q3U" + 
														"ThRd0tmYTBwWGE4WlZBeXVQbTRlbXFNb3NBRCtyYXdzcW14NWhKZm9ZaWZWZndMcnpocitSSmo1c3ZVNTdZdUdFWElsVGxE" + 
														"WEhmNytSYy9pS3B0SXhSaEVSVVpOLzlOQ2l6SVdSR3BBVmNTKy9XZXVvc3lqb2d6ZFZ6MkpZdWFaOGhEdE1YMFJFTStRclZ" + 
														"qeWRQUFJlejBodEtwOUJMbXh4QllSeEJPSEk1TEFPS0JRSXFOWm5Xd0JVbWpWdWVENDM0SWRFcFZpR01yTlNQV2kwZmUrTl" + 
														"dWSFBlc0JQd2poVnhJWkpFeXhuNDlxMTZSYXBuWisza2sxN3RqU2V4cm1pbUZxMUtqNTJFaFltektUSDRmQWdNQkFBR2pnZ" + 
														"1BhTUlJRDFqQ0J4d1lEVlIwU0JJRy9NSUc4aGhWb2RIUndPaTh2ZDNkM0xtbDZaVzV3WlM1amIyMkJEMmx1Wm05QWFYcGxi" + 
														"bkJsTG1OdmJhU0JrVENCampGSE1FVUdBMVVFQ2d3K1NWcEZUbEJGSUZNdVFTNGdMU0JEU1VZZ1FUQXhNek0zTWpZd0xWSk5" +
														"aWEpqTGxacGRHOXlhV0V0UjJGemRHVnBlaUJVTVRBMU5TQkdOaklnVXpneFF6QkJCZ05WQkFrTU9rRjJaR0VnWkdWc0lFMW" +
														"xaR2wwWlhKeVlXNWxieUJGZEc5eVltbGtaV0VnTVRRZ0xTQXdNVEF4TUNCV2FYUnZjbWxoTFVkaGMzUmxhWG93Z2RvR0ExV" +
														"WRFUVNCMGpDQno0RVNZWE5rZG1samRXNXVZVUJsYW1sbExtVnpwSUc0TUlHMU1VTXdRUVlKWUlWVUFRTUZBZ0lGRERSRVNW" +
														"SkZRME5KVDA0Z1JFVWdTVTVPVDFaQlEwbFBUaUJaSUVGRVRVbE9TVk5VVWtGRFNVOU9JRVZNUlVOVVVrOU9TVU5CTVJnd0Z" +
														"nWUpZSVZVQVFNRkFnSUREQWxUTkRnek16QXdNVU14TVRBdkJnbGdoVlFCQXdVQ0FnSU1Ja1ZWVTB0UElFcEJWVkpNUVZKSl" +
														"ZGcEJJQzBnUjA5Q1NVVlNUazhnVmtGVFEwOHhJVEFmQmdsZ2hWUUJBd1VDQWdFTUVuTmxiR3h2SUdWc1pXTjBjc096Ym1sa" +
														"mJ6QWRCZ05WSFNVRUZqQVVCZ2dyQmdFRkJRY0RBZ1lJS3dZQkJRVUhBd1F3SFFZRFZSME9CQllFRkNuajBXM1ZqYWhXd1VE" +
														"V29XNTdYMWgzZFQ5NU1COEdBMVVkSXdRWU1CYUFGRmF2LzVncFJ1Rlg1Y1dsbkhqSHhCSUpkSU5UTUlJQkhRWURWUjBnQkl" +
														"JQkZEQ0NBUkF3Z2dFTUJna3JCZ0VFQWZNNWFBUXdnZjR3SlFZSUt3WUJCUVVIQWdFV0dXaDBkSEE2THk5M2QzY3VhWHBsYm" +
														"5CbExtTnZiUzlqY0hNd2dkUUdDQ3NHQVFVRkJ3SUNNSUhIR29IRVFtVnliV1ZsYmlCdGRXZGhheUJsZW1GbmRYUjZaV3R2S" +
														"UhkM2R5NXBlbVZ1Y0dVdVkyOXRJRnBwZFhKMFlXZHBjbWxoYmlCcmIyNW1hV0Z1ZEhwaElHbDZZVzRnWVhWeWNtVjBhV3Nn" +
														"YTI5dWRISmhkSFZoSUdseVlXdDFjbkpwTGt4cGJXbDBZV05wYjI1bGN5QmtaU0JuWVhKaGJuUnBZWE1nWlc0Z2QzZDNMbWw" +
														"2Wlc1d1pTNWpiMjBnUTI5dWMzVnNkR1VnWld3Z1kyOXVkSEpoZEc4Z1lXNTBaWE1nWkdVZ1kyOXVabWxoY2lCbGJpQmxiQ0" +
														"JqWlhKMGFXWnBZMkZrYnpBNkJnZ3JCZ0VGQlFjQkFRUXVNQ3d3S2dZSUt3WUJCUVVITUFHR0htaDBkSEE2THk5dlkzTndaR" +
														"1Z6TG1sNlpXNXdaUzVqYjIwNk9EQTVOREFsQmdnckJnRUZCUWNCQXdRWk1CY3dDQVlHQkFDT1JnRUJNQXNHQmdRQWprWUJB" +
														"d0lCRHpBT0JnTlZIUThCQWY4RUJBTUNCUEF3T2dZRFZSMGZCRE13TVRBdm9DMmdLNFlwYUhSMGNEb3ZMMk55YkdSbGN5NXB" +
														"lbVZ1Y0dVdVkyOXRMMk5uYVMxaWFXNHZZM0pzYzJOaGNqSXdEUVlKS29aSWh2Y05BUUVGQlFBRGdnSUJBR08wUUxUYTRzWm" +
														"NjZTNQVjNON3dWdEpINVBDaW95c0NCMGhFY2VXN2lERXpBWWJWL3R6am5qTjJUYm95d0tLWmQyS2c0QkZOZlNVaDZ1by9Nb" +
														"CtIQ0FvWE45WGJxT0ZwNEdjb0FNNHg3NG8vYStjUTBRZ2RHMzlzb29KTnhsOC9jTUxGRk1JWmRQQzZtSWs5TUx5WTVMU3ow" +
														"SGp1QzVNdUowK2FCUzBBa1JhMTFDeGR0Y1IwQWhjc3VwaUJrLzZhRzFyTHViUC9FeUhMTDdEb1RVdlV1ekxhNzY5cFh2ZGp" +
														"KaUp6K28xVHhDNE1DWmExdnhadUROY1dLUHNEU0FFNVZ4VWJwc1ZwRjgwYys5MUc5NEV1RGUxa2tlSyt1cFdxRHl2UGdyRU" +
														"NjeUlTa2lPMWpFaWozY044QmN6NGMrbVFaQVlRSlhLQ1I0SVUyejVTTnA5dEN2ZUhBYS85RlNmQWs1eDl0WEJLOWEzL0E4b" +
														"E9vTVF4c2FCLzJpTjhmcFlwSjlGeVlJNUt0Zk10WDZNcFI5Nmh3Z0hXK05MSU84N1V4RUMwRGZtekxqbDBrYmVHYmVXcjY5" +
														"YXpmTkhuZDdZQ1lGUHVLQzNsSzZHampJR3VoVThXVUwwSC9uSlJQUE1XZUp5cE1uekZzaE8vYTl5cmpEY2lnMEExT0RnSWs" +
														"rSHI5VDBVR2QzUzg4dUg5dUJXSTZiMHB3K3VvVENTb240NHFkL3FxNHNSc0c2T0VKN3R0RVlxMkhxb0JEZXo5OU16UGk1ak" +
														"5YZlFtU3Q2NitHd3pNWnhiOXlUL1JuZWk1bkt3aHltc0hBOVg5VTNPdnBNM05NaDA1Sm1WcDQ3TXJxZERCdWt3ZjZEZTJuW" +
														"no5bndVNlZZUVM2Y1dMQ1o3VW1qRnA1REFtZkVqZk48L2RzaWc6WDUwOUNlcnRpZmljYXRlPg0KCQk8L2RzaWc6WDUwOURh" +
														"dGE+DQoJPC9kc2lnOktleUluZm8+DQoJPGRzaWc6T2JqZWN0IElkPSJJZDI3MTc0NjE3ODYzNTg2ODc5NzIxMzkwODc2MDg" +
														"iPg0KCQk8eGFkZXM6UXVhbGlmeWluZ1Byb3BlcnRpZXMgeG1sbnM6eGFkZXM9Imh0dHA6Ly91cmkuZXRzaS5vcmcvMDE5MD" +
														"MvdjEuMy4yIyIgVGFyZ2V0PSIjSWQxMTMwMTAxMzAxMjAwMjM2NDYwMTkyMTY3MjU0Ij4NCgkJCTx4YWRlczpTaWduZWRQc" +
														"m9wZXJ0aWVzIElkPSJJZDE5NTY4ODU4NTMyMDg0ODU1NzcwODM4NjkxNTU5Ij4NCgkJCQk8eGFkZXM6U2lnbmVkU2lnbmF0" +
														"dXJlUHJvcGVydGllcz4NCgkJCQkJPHhhZGVzOlNpZ25pbmdUaW1lPjIwMTUtMTAtMDVUMTM6MTI6NDNaPC94YWRlczpTaWd" +
														"uaW5nVGltZT4NCgkJCQkJPHhhZGVzOlNpZ25pbmdDZXJ0aWZpY2F0ZT4NCgkJCQkJCTx4YWRlczpDZXJ0Pg0KCQkJCQkJCT" +
														"x4YWRlczpDZXJ0RGlnZXN0Pg0KCQkJCQkJCQk8ZHNpZzpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczL" +
														"m9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIgeG1sbnM6ZHNpZz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2ln" +
														"IyIvPg0KCQkJCQkJCQk8ZHNpZzpEaWdlc3RWYWx1ZSB4bWxuczpkc2lnPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3h" +
														"tbGRzaWcjIj5QM28yQWp4UHpFY2N0UXBiSm4zd1N4V25sams9PC9kc2lnOkRpZ2VzdFZhbHVlPg0KCQkJCQkJCTwveGFkZX" +
														"M6Q2VydERpZ2VzdD4NCgkJCQkJCQk8eGFkZXM6SXNzdWVyU2VyaWFsPg0KCQkJCQkJCQk8ZHNpZzpYNTA5SXNzdWVyTmFtZ" +
														"SB4bWxuczpkc2lnPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj5DTj1DQSBwZXJzb25hbCBkZSBBQVBQ" +
														"IHZhc2NhcyAoMikgLSBERVNBUlJPTExPLCBPVT1BWlogWml1cnRhZ2lyaSBwdWJsaWtvYSAtIENlcnRpZmljYWRvIHB1Ymx" +
														"pY28gU0NBLCBPPUlaRU5QRSBTLkEuLCBDPUVTPC9kc2lnOlg1MDlJc3N1ZXJOYW1lPg0KCQkJCQkJCQk8ZHNpZzpYNTA5U2" +
														"VyaWFsTnVtYmVyIHhtbG5zOmRzaWc9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjIyNTY8L2RzaWc6W" +
														"DUwOVNlcmlhbE51bWJlcj4NCgkJCQkJCQk8L3hhZGVzOklzc3VlclNlcmlhbD4NCgkJCQkJCTwveGFkZXM6Q2VydD4NCgkJ" +
														"CQkJPC94YWRlczpTaWduaW5nQ2VydGlmaWNhdGU+DQoJCQkJCTx4YWRlczpTaWduYXR1cmVQb2xpY3lJZGVudGlmaWVyPg0" +
														"KCQkJCQkJPHhhZGVzOlNpZ25hdHVyZVBvbGljeUlkPg0KCQkJCQkJCTx4YWRlczpTaWdQb2xpY3lJZD4NCgkJCQkJCQkJPH" +
														"hhZGVzOklkZW50aWZpZXI+dXJuOmVqZ3Y6ZHNzOnBvbGljeToxPC94YWRlczpJZGVudGlmaWVyPg0KCQkJCQkJCQk8eGFkZ" +
														"XM6RG9jdW1lbnRhdGlvblJlZmVyZW5jZXM+DQoJCQkJCQkJCQk8eGFkZXM6RG9jdW1lbnRhdGlvblJlZmVyZW5jZT5odHRw" +
														"czovL2V1c2thZGkubmV0L2JvcHYyL2RhdG9zLzIwMTIvMDcvMTIwMzQ3NGEucGRmPC94YWRlczpEb2N1bWVudGF0aW9uUmV" +
														"mZXJlbmNlPg0KCQkJCQkJCQk8L3hhZGVzOkRvY3VtZW50YXRpb25SZWZlcmVuY2VzPg0KCQkJCQkJCTwveGFkZXM6U2lnUG" +
														"9saWN5SWQ+DQoJCQkJCQkJPHhhZGVzOlNpZ1BvbGljeUhhc2g+DQoJCQkJCQkJCTxkc2lnOkRpZ2VzdE1ldGhvZCBBbGdvc" +
														"ml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIiB4bWxuczpkc2lnPSJodHRwOi8vd3d3Lncz" +
														"Lm9yZy8yMDAwLzA5L3htbGRzaWcjIi8+DQoJCQkJCQkJCTxkc2lnOkRpZ2VzdFZhbHVlIHhtbG5zOmRzaWc9Imh0dHA6Ly9" +
														"3d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPmpvcklwMWJ2bWdJWFNCVE1CWWliakhCVHNCRT08L2RzaWc6RGlnZXN0Vm" +
														"FsdWU+DQoJCQkJCQkJPC94YWRlczpTaWdQb2xpY3lIYXNoPg0KCQkJCQkJCTx4YWRlczpTaWdQb2xpY3lRdWFsaWZpZXJzP" +
														"g0KCQkJCQkJCQk8eGFkZXM6U2lnUG9saWN5UXVhbGlmaWVyPg0KCQkJCQkJCQkJPHhhZGVzOlNQVVJJPmh0dHBzOi8vZXVz" +
														"a2FkaS5uZXQvYm9wdjIvZGF0b3MvMjAxMi8wNy8xMjAzNDc0YS5wZGY8L3hhZGVzOlNQVVJJPg0KCQkJCQkJCQk8L3hhZGV" +
														"zOlNpZ1BvbGljeVF1YWxpZmllcj4NCgkJCQkJCQk8L3hhZGVzOlNpZ1BvbGljeVF1YWxpZmllcnM+DQoJCQkJCQk8L3hhZG" +
														"VzOlNpZ25hdHVyZVBvbGljeUlkPg0KCQkJCQk8L3hhZGVzOlNpZ25hdHVyZVBvbGljeUlkZW50aWZpZXI+DQoJCQkJPC94Y" +
														"WRlczpTaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPg0KCQkJPC94YWRlczpTaWduZWRQcm9wZXJ0aWVzPg0KCQkJPHhhZGVz" +
														"OlVuc2lnbmVkUHJvcGVydGllcz4NCgkJCQk8eGFkZXM6VW5zaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPg0KCQkJCQk8eGF" +
														"kZXM6U2lnbmF0dXJlVGltZVN0YW1wPg0KCQkJCQkJPGRzaWc6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Im" +
														"h0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPg0KCQkJCQkJPHhhZGVzOkVuY2Fwc3VsYXRlZFRpb" +
														"WVTdGFtcD5NSUlURUFZSktvWklodmNOQVFjQ29JSVRBVENDRXYwQ0FRTXhDekFKQmdVckRnTUNHZ1VBTUhrR0N5cUdTSWIz" +
														"RFFFSkVBRUVvR29FYURCbUFnRUJCZ2tyQmdFRUFmTTVad013SVRBSkJnVXJEZ01DR2dVQUJCVDgyUmZlSy9HWmdxWk9rYUF" +
														"BNnljRW13dE9iZ0lRSFY1RXU3MVpkdkpXRW5kTS92blVzQmdQTWpBeE5URXdNRFV4TXpFeU5EUmFBaEIvTVBqOUh6ZE5KbF" +
														"lTZDB0WDFBMk9vSUlPK3pDQ0J3TXdnZ1Ryb0FNQ0FRSUNFQXQ5UUpVc3VjRFpTWWhmR2xyaGh4c3dEUVlKS29aSWh2Y05BU" +
														"UVGQlFBd1JURUxNQWtHQTFVRUJoTUNSVk14RkRBU0JnTlZCQW9NQzBsYVJVNVFSU0JUTGtFdU1TQXdIZ1lEVlFRRERCZEpl" +
														"bVZ1Y0dVdVkyOXRJQzBnUkVWVFFWSlNUMHhNVHpBZUZ3MHdPVEF5TURNeE5URXpNekJhRncwek56RXhNamd5TXpBd01EQmF" +
														"NRk14Q3pBSkJnTlZCQVlUQWtWVE1SUXdFZ1lEVlFRS0RBdEpXa1ZPVUVVZ1V5NUJMakV1TUN3R0ExVUVBd3dsUTBFZ1ZHVn" +
														"JibWxyYjJFZ0xTQkRRU0JVWldOdWFXTmhJQzBnUkVWVFFWSlNUMHhNVHpDQ0FpSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnS" +
														"VBBRENDQWdvQ2dnSUJBT0hOanVXQ3UvM0NqR1pMb3haNlI4RDFwR1o3azlkem5NUlVCS0NrN1NDMFoyUklZQUM1Rk1QMElv" +
														"d1QybzQzc0tnWDRiZU9zUk9YR0h6aTRNVm92cnFvdUFkM0NmZjlwTkhaeGVGcUFVeTg1LyszMno2OFk5RGR3SDZya3d4eEk" +
														"rdGtpeTc3czdCQkVrak1RZ3RHRXJQODBPd1NLVG9lcWNLYXk5bENCRDZoUUpjTEF1NFpxbVBDY2tQTlNmVGNSY0RjcmFJTT" +
														"IyVDdZcG9qdkdNYjNYMnQzcE56YUY2bDBVV2pQTXZzTVlUK3NNZUdpTmVMV0pCbjM5OE8xU3VOM1FzR2s2ODU4REx2Q1h4b" +
														"HFZSGVIaXl6MGJ0eFJ1QTZBNGUySzZ0WFYrWktwcUhlVENTZU00VXVVdmdHT3FxSVpEYlF5Y2R6SUtnVXlYZjNQR0tTUUdI" +
														"N1hzck93cGsvRUJLVWM4SnNuekxyTzFvYkc1OE10YWxUSWpGMlB6cGowOXMwTVl2UE1wSmRuMXhDZUlzd0hlS3k2a1F0V3R" +
														"sVGhVZjM4YXdJb0pUTkwyUFdCb1hMeFhpc0JoSmE4MkxmaGYzN0RpSEduakZha2o2bXpSelF1dTdmUWVwM0c2VEVwd0ZLTF" +
														"ZLV2xQODdMakxHMVlySnJsTjVkZW1iUlNkVmRSbkZxaGtsRUc4aDBUYkp5eVdvN1ZXa1M5a1Q3SDJod2IzQ1hFV1crVEwvM" +
														"jJqTzNMeXVuSnRIWk95cDgrQWQwVDFjT3lIZEJoQlVQRDgxYzZncnQySEpMSnRhQ09pdTF0NjRzUFM1NitqWDFDQWgzZlFR" +
														"MDZuVHdQbzNlMWpHcjJJRmxjOVF6TjB0VklVRGZ1em43bEE3bHVKZkQ5cnFzZUZyVk1PZnU2cDNBZ01CQUFHamdnSGZNSUl" +
														"CMnpDQnh3WURWUjBSQklHL01JRzhoaFZvZEhSd09pOHZkM2QzTG1sNlpXNXdaUzVqYjIyQkQybHVabTlBYVhwbGJuQmxMbU" +
														"52YmFTQmtUQ0JqakZITUVVR0ExVUVDZ3crU1ZwRlRsQkZJRk11UVM0Z0xTQkRTVVlnUVRBeE16TTNNall3TFZKTlpYSmpMb" +
														"FpwZEc5eWFXRXRSMkZ6ZEdWcGVpQlVNVEExTlNCR05qSWdVemd4UXpCQkJnTlZCQWtNT2tGMlpHRWdaR1ZzSUUxbFpHbDBa" +
														"WEp5WVc1bGJ5QkZkRzl5WW1sa1pXRWdNVFFnTFNBd01UQXhNQ0JXYVhSdmNtbGhMVWRoYzNSbGFYb3dEd1lEVlIwVEFRSC9" +
														"CQVV3QXdFQi96QU9CZ05WSFE4QkFmOEVCQU1DQVFZd0hRWURWUjBPQkJZRUZHTytlQThsaHlYYlFvUTU1SWdjMlhIZTZWU0" +
														"xNQjhHQTFVZEl3UVlNQmFBRkxPNlpjZjlROFhiL0lmWjlYL0RualNiKzJ0Tk1Eb0dBMVVkSUFRek1ERXdMd1lFVlIwZ0FEQ" +
														"W5NQ1VHQ0NzR0FRVUZCd0lCRmhsb2RIUndPaTh2ZDNkM0xtbDZaVzV3WlM1amIyMHZZM0J6TURvR0NDc0dBUVVGQndFQkJD" +
														"NHdMREFxQmdnckJnRUZCUWN3QVlZZWFIUjBjRG92TDI5amMzQmtaWE11YVhwbGJuQmxMbU52YlRvNE1EazBNRFlHQTFVZEh" +
														"3UXZNQzB3SzZBcG9DZUdKV2gwZEhBNkx5OWpjbXhrWlhNdWFYcGxibkJsTG1OdmJTOWpaMmt0WW1sdUwyRnliREl3RFFZSk" +
														"tvWklodmNOQVFFRkJRQURnZ0lCQUhVL3FZY0d2Ym81U1VmYThOYUVsL3ozdWY2U2hkMm9yekRubytxclNCYWVMTGlkTHhVO" +
														"UtYZy92RjQxbWZ5SzR4NmdQS3ZBTEs0RGlDa05hQ01NWm54OFhNUkhoaWZKMmdFTitLcFVZRjhRQ3RaMWpRTkVqelhYVGw4" +
														"TnMvYzJsbTl5dkNkRDBiZVE2LzMzTUdTWnczLzJYd2dJMlZNU2JiU2ZJVUhwa2hZMWRjMk5JZ0F1eHVpRFBzbEZ6QmF3US8" +
														"4SXhNMjAwRTlCcmNtS3NIRUt1dHJmL3JMN05ET01sUHdibjNvc3A4N2NWYTdSZ0I1cWJRb2Q2eUZaa3RGUmdsQlRtN0Jkaj" +
														"hvdm9jVDdYTnhONHhVUkRBVkFVZlpvY3NPVWlUbzY0ZVRGRWQ4YnUwUlRxdGl1R2pUM2RqelJ2NVNZcnlxM0hBV3E5NGhyc" +
														"jRzR1JOc3p5aE15aTYxMDZabUJHVlR4eEdkdm11WERNQm5vcXNxUkxwaDRwcFZlTk9NOXkxSGorVnREQnZ3NUpiM3QyZTNT" +
														"L2QyMDZBRXd5Q2ZyTnFiUGlGYlp5TmdtZ1E1RnZsMks0ZkFVRVJuajloSlFjNTVMeTgraWJIaVJPRmF6ZXE2OE8xL3YwSmd" +
														"jd0ZQdi9IUWNwV1VQTlk5NVczaWFKZUlEeWRsaUczY1JVc3dHTHp3aDI1WDNJRnI5L0xuaG5XaHV0SFhuUzFJbHlGT1hkdm" +
														"hSL2EyVWQ4WXIzTGxyZHFDMndYTk1qRWpPVitoRHZwTkpSZ2NqWjcxTlNMS2RBRWIvTHlUQ0JoRVF6R2dOTTRIZlRHendPS" +
														"Ed3WEx4M2N0MGtRL2NPS1dwa3RGbGZlOGdmamExdFQzK0YyOGlFaUhxV2xVZFAya0FYbURvSEJMeHhGY0tBTUlJSDhEQ0NC" +
														"ZGlnQXdJQkFnSUNBTm93RFFZSktvWklodmNOQVFFTEJRQXdVekVMTUFrR0ExVUVCaE1DUlZNeEZEQVNCZ05WQkFvTUMwbGF" +
														"SVTVRUlNCVExrRXVNUzR3TEFZRFZRUUREQ1ZEUVNCVVpXdHVhV3R2WVNBdElFTkJJRlJsWTI1cFkyRWdMU0JFUlZOQlVsSl" +
														"BURXhQTUI0WERURXlNRGt4TnpFeE16ZzFNMW9YRFRFM01Ea3hOekV4TXpnMU0xb3dUREVMTUFrR0ExVUVCaE1DUlZNeEZEQ" +
														"VNCZ05WQkFvTUMwbGFSVTVRUlNCVExrRXVNU2N3SlFZRFZRUUREQjUwYzJGa1pYTXVhWHBsYm5CbExtTnZiU0F0SUVSRlUw" +
														"RlNVazlNVEU4d2dnSWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUNEd0F3Z2dJS0FvSUNBUURiVkZlQmRPaUJ6aVlvUXdRN3p" +
														"3bnlpeVBHcVFYVUlzaXduTVhNTVhLSEZsNGExRTg1TG9kcmtXVVlDS1dSZXRIamF6V1hyc0xHOS9ZSkRSbXVxQ01aelJ5K2" +
														"FoVmN0ZXFOeW1YNjhyYWV4UmlpR0FBaGlOclAwc3Z1VXh0T0wwZi84RXh5S1Zvc25EM1FQZDZodnJkWjdDRjIwTnRmNWUzV" +
														"mNWK3VoWjZtL1dTNW5wMjlsSjJzQVJYaDhxSU11TEVWT2ZhMWZvcTlrTzJ2OVNhdEdJM0N1aG1RWmZqL0U1UFVCT3c4b2FI" +
														"Rk5YL1AydzFheUhJUWJJdUwvUnZLc2JDejJsN1NYTzdDbXRjYmduT0pkRm9CRVJ0QW1rMHZ6VEdrY1VocUJyZlo3My91Qng" +
														"vLzhMa0pzZEE5SUxaMUcra2x5cXJtd3VpYkN5cVJHaENleWZITG5UeFBMUCtNMXFiZWpmdFkxaklMNHZhQjcyeHdXMnArRH" +
														"dxL1U3d1F6QzAyQWdqWEVPMDhQTEljcTRhVlBZekNweFJmZ1VaVEJYTHZQdkZEL2tma3pMbUg5TmlhZzcvMzZUZVdxclFIO" +
														"GtOQ1dGY2hWNVBLbTYydDhuRVoyb1Q5bERoelpyKzFFaHJoKzFXWHB2YVF6YkRuTHNqODJORFplWWtFUER0NUwydE1CeXVL" +
														"MGMzYXlZUklqeFN5NWdWYnZFb2tVTzVCdGYydDVSc0Y4MUV1aGNPbTZyZFpBZUhVQ1J4RXp5NS9ZL045dklnR0dqdmtnbDk" +
														"rcktiWU5VYkhwemZZbHNCRnFiY2FCcXgzSGFoS3h5M2ZhRVVmRkhvcTdjMUZTbkE5SDdkMnBCdUQyOTgrNmZaSU1sYzhmS0" +
														"FXc3ByZmhMN3JMaE1oVlg2R044YlUzUUlEQVFBQm80SUMwekNDQXM4d2djY0dBMVVkRWdTQnZ6Q0J2SVlWYUhSMGNEb3ZMM" +
														"2QzZHk1cGVtVnVjR1V1WTI5dGdROXBibVp2UUdsNlpXNXdaUzVqYjIya2daRXdnWTR4UnpCRkJnTlZCQW9NUGtsYVJVNVFS" +
														"U0JUTGtFdUlDMGdRMGxHSUVFd01UTXpOekkyTUMxU1RXVnlZeTVXYVhSdmNtbGhMVWRoYzNSbGFYb2dWREV3TlRVZ1JqWXl" +
														"JRk00TVVNd1FRWURWUVFKRERwQmRtUmhJR1JsYkNCTlpXUnBkR1Z5Y21GdVpXOGdSWFJ2Y21KcFpHVmhJREUwSUMwZ01ERX" +
														"dNVEFnVm1sMGIzSnBZUzFIWVhOMFpXbDZNQTRHQTFVZER3RUIvd1FFQXdJSGdEQVdCZ05WSFNVQkFmOEVEREFLQmdnckJnR" +
														"UZCUWNEQ0RBZEJnTlZIUTRFRmdRVTZkNHpVT1pscUR6ODF5R0lVNGFUaDJQa212QXdId1lEVlIwakJCZ3dGb0FVWTc1NER5" +
														"V0hKZHRDaERua2lCelpjZDdwVklzd09nWUlLd1lCQlFVSEFRc0VMakFzTUNvR0NDc0dBUVVGQnpBRGhoNW9kSFJ3T2k4dmR" +
														"ITmhaR1Z6TG1sNlpXNXdaUzVqYjIwNk9EQTVNeTh3Z2dFZUJnTlZIU0FFZ2dFVk1JSUJFVENDQVEwR0NTc0dBUVFCOHpsbk" +
														"F6Q0IvekFsQmdnckJnRUZCUWNDQVJZWmFIUjBjRG92TDNkM2R5NXBlbVZ1Y0dVdVkyOXRMMk53Y3pDQjFRWUlLd1lCQlFVS" +
														"EFnSXdnY2dhZ2NWQ1pYSnRaV1Z1SUcxMVoyRnJJR1Y2WVdkMWRIcGxhMjhnZDNkM0xtbDZaVzV3WlM1amIyMGdXbWwxY25S" +
														"aFoybHlhV0Z1SUd0dmJtWnBZVzUwZW1FZ2FYcGhiaUJoZFhKeVpYUnBheUJyYjI1MGNtRjBkV0VnYVhKaGEzVnljbWt1SUV" +
														"4cGJXbDBZV05wYjI1bGN5QmtaU0JuWVhKaGJuUnBZWE1nWlc0Z2QzZDNMbWw2Wlc1d1pTNWpiMjBnUTI5dWMzVnNkR1VnWl" +
														"d3Z1kyOXVkSEpoZEc4Z1lXNTBaWE1nWkdVZ1kyOXVabWxoY2lCbGJpQmxiQ0JqWlhKMGFXWnBZMkZrYnpBOUJnTlZIUjhFT" +
														"mpBME1ES2dNS0F1aGl4b2RIUndPaTh2WTNKc1pHVnpMbWw2Wlc1d1pTNWpiMjB2WTJkcExXSnBiaTlqY214MFpXTnVhV05o" +
														"TWpBTkJna3Foa2lHOXcwQkFRc0ZBQU9DQWdFQXpOVDJDdm5HQnRpK0dDbzNqb1FtNWVBYTd3Um9FWFh1dGxURUNHVEIzNFZ" +
														"1eGVYMnQyMUlmMEVaeDltQ2xESGxueENObEFFQSttZmJiY1ZRMlFuaHhWeGVxMlBGMk5ZdTBBRnEreVBUV2s0aWpRRzlXL1" +
														"pYTnhiMjNMN3lGNUlsamhKR2tsK0JFRWVBM1JEbTl5MlR1U3RpanpFeTFWa1RIOW9NL2I4M1RWaVBmUTVXWW1nMENjS0J3b" +
														"GpHdHpWQTYzSTRrblI4YnlmdTByVVFaZ2lkcVdFQ2RBMW02NFVlVXNnei9Ib1JTUUlha216cGlZQkxBRW9wZ2RKL1E4ME1z" +
														"SnFRZkhRbWc0NFh6NWNCS2x5N3pVOFE0Ukh3WEFtckZ1L2lsSTZtZmY2ZnpFbU5XSnVMaTgzS29yUHlkbkJEYzJiZDNCRy9" +
														"hdzNiMkNid0xEU0pJMnc5YldocTB4Y0lUYXV0RTZ1NDBKQnFSSy8xYXJlNUZEbXRJNnJzZ0dHQjBlU3ZraTJxVXlpemJObE" +
														"lmZ2lvZjlWU3VGSFVXUWNLZzZxeWcxeGJPZDBQUmNCaTJSSnNuckZyaVRPdFhlYnE2OTRpZWgzSXhyUkY3c2dZYnZLUHJiV" +
														"mVtSXl0WStoRml0ZEZMS3Q2b3pjVDhvaW1Rd0NoaXppNzJkY1doQkUzWElrcERRbEFTRksxMVJUcHA3dWpiZlZiTlF1TTdW" +
														"clEwVjluUkt6RkpkWmNZbGtCTkdpUWpDRHdrOWY1dFBYVm93MEhxSlFFNlcrdTg2YXh0Ryt2SkoxSWVzWjJoWGVsbWNONyt" +
														"6elcvUGxxMWxiUU54alVPM210RUZ2SWFQc1dSOEpqb2g1TVB1K3FmTi8wRTl2RTFtdEpjbjMvS2wwTGIrVDBLV1pHYjU0eG" +
														"dnTnZNSUlEYXdJQkFUQlpNRk14Q3pBSkJnTlZCQVlUQWtWVE1SUXdFZ1lEVlFRS0RBdEpXa1ZPVUVVZ1V5NUJMakV1TUN3R" +
														"0ExVUVBd3dsUTBFZ1ZHVnJibWxyYjJFZ0xTQkRRU0JVWldOdWFXTmhJQzBnUkVWVFFWSlNUMHhNVHdJQ0FOb3dDUVlGS3c0" +
														"REFob0ZBS0NCN0RBYUJna3Foa2lHOXcwQkNRTXhEUVlMS29aSWh2Y05BUWtRQVFRd0hBWUpLb1pJaHZjTkFRa0ZNUThYRFR" +
														"FMU1UQXdOVEV6TVRJME5Gb3dJd1lKS29aSWh2Y05BUWtFTVJZRUZDc2tIMDltS3UzNis1aDJNOHlwK3EweU95VUFNSUdLQm" +
														"dzcWhraUc5dzBCQ1JBQ0RERjdNSGt3ZHpCMUJCUkgyaFpXV2V4a3QxQTJXSzQxbE5NajRxYk5CVEJkTUZla1ZUQlRNUXN3Q" +
														"1FZRFZRUUdFd0pGVXpFVU1CSUdBMVVFQ2d3TFNWcEZUbEJGSUZNdVFTNHhMakFzQmdOVkJBTU1KVU5CSUZSbGEyNXBhMjlo" +
														"SUMwZ1EwRWdWR1ZqYm1sallTQXRJRVJGVTBGU1VrOU1URThDQWdEYU1BMEdDU3FHU0liM0RRRUJBUVVBQklJQ0FET2tieFp" +
														"KN2pGTVNRRHZMOFhrSi9XbDF3VllVamVuL3cvbmJ0RVVBWkIwT21yL0dsU2xrVE9QdENiTEpZeGhodnhUaTUzNzRQT1VlRn" +
														"JpRCtDd2ZBNldYaEhkL1ZKNXdWRVIyOEI5bDhWQUVoclRLZzNpb3U1VTEzcEdic1dCYmhxS2FiaWlWSUpBUUJjTUt0ejhNS" +
														"0taa0Vhang0RGdBNWEvUzdxWCtBVitoMnBCcmpmazJBMG9MV01BQzNrUm1CWmRKQkgxK3dtYkc5eEdyUEpFZ1Q3WGxFVkVR" +
														"cVJ0cXl1NHVzaXh6SDBlbkRLc3NkUVlnaU1VZVVQdG1oK3pVd29qTzlnay9CVi9mSlJ5WkpvU3hQUkZqcjFBYWZoZlcySkV" +
														"lOGh4d29VSks5WlN0cDVKWHM5L1UwNGJQVXhDa3BYOEJPcU5BNVJqR05YM3JDdVVkY0pKZndLWjR3cERYTHluVkRDV3ZVYk" +
														"RHa3RhSDAwZ0R1RWVFMmllckhoMVhjeHluWUlFenMvREJ2M3daN2paRFhaMjYrRXR1KzQ3Y2xjWDV3aTFyK0pMTnd1SUN0V" +
														"XVlYXgzRy85aTIxTXo2UElnRXRISUp6REtkQU8wdGNBRXB0cWF5RjFFR2hOeERNcUFSQU8wWnVObzM3akMwWUcwRExyTGlq" +
														"MldoMDBPZWRCekNxeE4wU0IrZkpGWHhyTVRtNG9RSmtrK09yTXBub1BTNnlZOTkzZ2F3U0VvTFg2bHVjdjNsTVVjVXl2QWt" +
														"QYWFibHpiL2JZcmpGc0p2ZlhCbk5WSzJ0L040N053RnNjMmFTckZNaEpLVnlhVFc4dWNKMTdZcGdHWEJvYkVRVnYzQUpiYV" +
														"F0WGZkaHdaWlhEZ1JXdGk5cTNBSzNwTFovQjJybUF4cFFlQTwveGFkZXM6RW5jYXBzdWxhdGVkVGltZVN0YW1wPg0KCQkJC" +
														"Qk8L3hhZGVzOlNpZ25hdHVyZVRpbWVTdGFtcD4NCgkJCQk8L3hhZGVzOlVuc2lnbmVkU2lnbmF0dXJlUHJvcGVydGllcz4N" +
														"CgkJCTwveGFkZXM6VW5zaWduZWRQcm9wZXJ0aWVzPg0KCQk8L3hhZGVzOlF1YWxpZnlpbmdQcm9wZXJ0aWVzPg0KCTwvZHN" +
														"pZzpPYmplY3Q+DQo8L2RzaWc6U2lnbmF0dXJlPg==";
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
			@Override
			public SignatureVerifyOutputData verifyXAdESSignature(final byte[] signedData,
															      final byte[] signature) {
				return new SignatureVerifyOutputData(new VerificationResult("true", 
																			 null, null, null, 
																			 null, null, null, null, 
																			 null, null, null));
			}
			@Override
			public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,
																  final String signature) {
				return new SignatureVerifyOutputData(new VerificationResult("true", 
						 													null, null, null, 
						 													null, null, null, null, 
						 													null, null, null));
			}
			@Override
			public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,
																  final Document signature) {
				return new SignatureVerifyOutputData(new VerificationResult("true", 
						 													null, null, null, 
						 													null, null, null, null, 
						 													null, null, null));
			}
			@Override
			public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData,
																  final Document signature) throws IOException {
				return new SignatureVerifyOutputData(new VerificationResult("true", 
						 												    null, null, null, 
						 												    null, null, null, null, 	
						 												    null, null, null));
			}
			@Override
			public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData, 
																  final InputStream signature) throws IOException {
				return new SignatureVerifyOutputData(new VerificationResult("true", 
						 													null, null, null, 
						 													null, null, null, null, 
						 													null, null, null));
			}
			@Override
			public SignatureRequestOutputData createXAdESSignatureOf(final URL urlToBeSigned) throws IOException {
				EjgvDocument ejgvDocument = new EjgvDocument();
				ejgvDocument.setHeader(new Header());
				ejgvDocument.setBody(new Body());
				
				ejgvDocument.getHeader().setType("xades");
				ejgvDocument.getHeader().setPlacement("detached");
				ejgvDocument.getHeader().setFormat("ES-T");
				ejgvDocument.getHeader().setVersion("2.0");
				ejgvDocument.getHeader().setFlags("0");
				ejgvDocument.getHeader().setIsConservable("1");
				ejgvDocument.getHeader().setDocumentIsRequired("0");
				ejgvDocument.getBody().setSign(mockSignature);
				
				return new SignatureRequestOutputData(ejgvDocument);
			}
			
			@Override
			public SignatureRequestOutputData createXAdESSignatureOf(final File fileToBeSigned) throws IOException {
				EjgvDocument ejgvDocument = new EjgvDocument();
				ejgvDocument.setHeader(new Header());
				ejgvDocument.setBody(new Body());
				
				ejgvDocument.getHeader().setType("xades");
				ejgvDocument.getHeader().setPlacement("detached");
				ejgvDocument.getHeader().setFormat("ES-T");
				ejgvDocument.getHeader().setVersion("2.0");
				ejgvDocument.getHeader().setFlags("0");
				ejgvDocument.getHeader().setIsConservable("1");
				ejgvDocument.getHeader().setDocumentIsRequired("0");
				ejgvDocument.getBody().setSign(mockSignature);
				
				return new SignatureRequestOutputData(ejgvDocument);
			}
			@Override
			public SignatureRequestOutputData createXAdESSignatureOf(final byte[] dataToBeSigned) {
				EjgvDocument ejgvDocument = new EjgvDocument();
				ejgvDocument.setHeader(new Header());
				ejgvDocument.setBody(new Body());
				
				ejgvDocument.getHeader().setType("xades");
				ejgvDocument.getHeader().setPlacement("detached");
				ejgvDocument.getHeader().setFormat("ES-T");
				ejgvDocument.getHeader().setVersion("2.0");
				ejgvDocument.getHeader().setFlags("0");
				ejgvDocument.getHeader().setIsConservable("1");
				ejgvDocument.getHeader().setDocumentIsRequired("0");
				ejgvDocument.getBody().setSign(mockSignature);
				
				return new SignatureRequestOutputData(ejgvDocument);
			}
			
			@Override
			public SignatureRequestOutputData createXAdESSignatureOf(final InputStream dataToBeSigned) throws IOException {
				EjgvDocument ejgvDocument = new EjgvDocument();
				ejgvDocument.setHeader(new Header());
				ejgvDocument.setBody(new Body());
				
				ejgvDocument.getHeader().setType("xades");
				ejgvDocument.getHeader().setPlacement("detached");
				ejgvDocument.getHeader().setFormat("ES-T");
				ejgvDocument.getHeader().setVersion("2.0");
				ejgvDocument.getHeader().setFlags("0");
				ejgvDocument.getHeader().setIsConservable("1");
				ejgvDocument.getHeader().setDocumentIsRequired("0");
				ejgvDocument.getBody().setSign(mockSignature);
				
				return new SignatureRequestOutputData(ejgvDocument);
			}
			@Override
			public SignatureRequestOutputData createXAdESSignatureOf(final String dataToBeSigned) {
				EjgvDocument ejgvDocument = new EjgvDocument();
				ejgvDocument.setHeader(new Header());
				ejgvDocument.setBody(new Body());
				
				ejgvDocument.getHeader().setType("xades");
				ejgvDocument.getHeader().setPlacement("detached");
				ejgvDocument.getHeader().setFormat("ES-T");
				ejgvDocument.getHeader().setVersion("2.0");
				ejgvDocument.getHeader().setFlags("0");
				ejgvDocument.getHeader().setIsConservable("1");
				ejgvDocument.getHeader().setDocumentIsRequired("0");
				ejgvDocument.getBody().setSign(mockSignature);
				
				return new SignatureRequestOutputData(ejgvDocument);
			}
		};
	}
}
