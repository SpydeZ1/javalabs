package org.example.service;

import org.example.model.EditorialComment;
import org.example.repository.EditorialCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EditorialCommentService {

    @Autowired
    private EditorialCommentRepository commentRepository;

    public List<EditorialComment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<EditorialComment> getCommentsByTranslationId(Long translationId) {
        return commentRepository.findByTranslationId(translationId);
    }

    public Optional<EditorialComment> getCommentById(Long id) {
        return Optional.ofNullable(commentRepository.findById(id));
    }

    public EditorialComment createComment(EditorialComment comment) {
        return commentRepository.save(comment);
    }

    public EditorialComment updateComment(Long id, EditorialComment commentDetails) {
        EditorialComment comment = commentRepository.findById(id);
        if (comment != null) {
            comment.setTranslationId(commentDetails.getTranslationId());
            comment.setCommentText(commentDetails.getCommentText());
            comment.setStatus(commentDetails.getStatus());
            return commentRepository.save(comment);
        }
        return null;
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public boolean commentExists(Long id) {
        return commentRepository.existsById(id);
    }
}