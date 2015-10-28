package r01f.httpclient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.httpclient.HttpRequestFormParameterForMultiPartBinaryData.HttpRequestFormBinaryParameterTransferEncoding;
import r01f.mime.MimeType;


@Accessors(prefix="_")
abstract class HttpRequestPayloadBase<SELF_TYPE extends HttpRequestPayloadBase<SELF_TYPE>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter @Setter(AccessLevel.PACKAGE) private byte[] _content;
	@Getter @Setter(AccessLevel.PACKAGE) private MimeType _mimeType = MimeType.OCTECT_STREAM;	// default
	@Getter @Setter(AccessLevel.PACKAGE) private HttpRequestFormBinaryParameterTransferEncoding _transferEncoding = HttpRequestFormBinaryParameterTransferEncoding.BINARY;
	
			@Setter(AccessLevel.PACKAGE) private transient boolean _mustEncodeToTargetServerCharset = false;
			
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-APIs
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public SELF_TYPE mimeType(final MimeType mime) {
		_mimeType = mime;
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE transferedEncodedAs(final HttpRequestFormBinaryParameterTransferEncoding transferEncoding) {
		_transferEncoding = transferEncoding;
		return (SELF_TYPE)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public boolean mustEncodeToTargetServerCharset() {
		return _mustEncodeToTargetServerCharset;
	}
}