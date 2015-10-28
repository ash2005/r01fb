package r01f.model.jobs;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Models a job enqueued to be background-processed 
 */
@XmlRootElement(name="enqueuedJobStatus")
@Accessors(prefix="_")
public class EnqueuedJobCurrentStatus 
     extends EnqueuedJobBase {

	private static final long serialVersionUID = -3386912588270099640L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * any data about the progress
	 * (could be a percentage or a concrete number)
	 */
	@XmlElement(name="progress")
	@Getter @Setter private long _progress;
	/**
	 * any data about the remaining data to be processed
	 * (could be a percentage or a concrete number... obviously if it's a percentage, 
	 *  if progress=80, then remaining=20) 
	 */
	@XmlElement(name="remaining")
	@Getter @Setter private long _remaining;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJobCurrentStatus() {
		// nothing
	}
	public EnqueuedJobCurrentStatus(final EnqueuedJobOID jobOid,final Date enqueuedDate,
									final EnqueuedJobStatus status,
									final long progress,final long remaining,
									final String detail) {
		super(jobOid,enqueuedDate,
			  status,
			  detail);
		_progress = progress;
		_remaining = remaining;
	}
	public EnqueuedJobCurrentStatus(final EnqueuedJob job,
									final EnqueuedJobStatus status,
									final long progress,final long remaining,
									final String detail) {
		super(job.getOid(),job.getEnqueuedTimeStamp(),
			  status);
		_progress = progress;
		_remaining = remaining;
		_detail = detail;
		
	}
			
}
