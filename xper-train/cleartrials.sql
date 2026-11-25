DELETE t FROM TaskToDo t
WHERE t.task_id > 0
  AND NOT EXISTS (
        SELECT 1 FROM TaskDone d
        WHERE d.task_id = t.task_id
          AND d.part_done = 0
  );