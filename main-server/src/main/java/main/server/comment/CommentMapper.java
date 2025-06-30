package main.server.comment;

import main.server.comment.dto.CommentDto;
import main.server.comment.dto.NewCommentDto;
import main.server.comment.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "created", source = "createdOn")
    @Mapping(target = "updated", source = "updatedOn")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    Comment toComment(NewCommentDto commentDto);
}
