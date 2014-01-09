package models.beans;

public class Review {
	
	public String nick;
	public String review;
	public String reviewDate;
	
	public Review() {
	}
	
	public Review(String nick, String review, String reviewDate) {
		this.nick = nick;
		this.review = review;
		this.reviewDate = reviewDate;
	}
	
	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getReview() {
		return review;
	}
	public void setReview(String review) {
		this.review = review;
	}
	public String getReviewDate() {
		return reviewDate;
	}
	public void setReviewDate(String reviewDate) {
		this.reviewDate = reviewDate;
	}
	

}
