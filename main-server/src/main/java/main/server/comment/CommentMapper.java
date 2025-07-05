package main.server.comment;

import main.server.comment.dto.CommentDto;
import main.server.comment.dto.NewCommentDto;
import main.server.comment.model.Comment;
import main.server.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "created", source = "createdOn")
    @Mapping(target = "updated", source = "updatedOn")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Comment toComment(NewCommentDto commentDto);
}