package communication;

public enum BistroResponseStatus {
    SUCCESS,
    FAILURE,
    INVALID_REQUEST,
    NOT_FOUND,
    ALREADY_EXISTS,
    NOT_ALLOWED,     
    NO_AVAILABLE_TABLE, 
    DB_ERROR,           
    SERVER_ERROR       
}
