<?php
namespace ;

/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
use Thrift\Base\TBase;
use Thrift\Type\TType;
use Thrift\Type\TMessageType;
use Thrift\Exception\TException;
use Thrift\Exception\TProtocolException;
use Thrift\Protocol\TProtocol;
use Thrift\Protocol\TBinaryProtocolAccelerated;
use Thrift\Exception\TApplicationException;


final class JobTrackerState {
  const INITIALIZING = 1;
  const RUNNING = 2;
  static public $__names = array(
    1 => 'INITIALIZING',
    2 => 'RUNNING',
  );
}

class HiveClusterStatus {
  static $_TSPEC;

  /**
   * @var int
   */
  public $taskTrackers = null;
  /**
   * @var int
   */
  public $mapTasks = null;
  /**
   * @var int
   */
  public $reduceTasks = null;
  /**
   * @var int
   */
  public $maxMapTasks = null;
  /**
   * @var int
   */
  public $maxReduceTasks = null;
  /**
   * @var int
   */
  public $state = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'taskTrackers',
          'type' => TType::I32,
          ),
        2 => array(
          'var' => 'mapTasks',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'reduceTasks',
          'type' => TType::I32,
          ),
        4 => array(
          'var' => 'maxMapTasks',
          'type' => TType::I32,
          ),
        5 => array(
          'var' => 'maxReduceTasks',
          'type' => TType::I32,
          ),
        6 => array(
          'var' => 'state',
          'type' => TType::I32,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['taskTrackers'])) {
        $this->taskTrackers = $vals['taskTrackers'];
      }
      if (isset($vals['mapTasks'])) {
        $this->mapTasks = $vals['mapTasks'];
      }
      if (isset($vals['reduceTasks'])) {
        $this->reduceTasks = $vals['reduceTasks'];
      }
      if (isset($vals['maxMapTasks'])) {
        $this->maxMapTasks = $vals['maxMapTasks'];
      }
      if (isset($vals['maxReduceTasks'])) {
        $this->maxReduceTasks = $vals['maxReduceTasks'];
      }
      if (isset($vals['state'])) {
        $this->state = $vals['state'];
      }
    }
  }

  public function getName() {
    return 'HiveClusterStatus';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->taskTrackers);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->mapTasks);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->reduceTasks);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->maxMapTasks);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 5:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->maxReduceTasks);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 6:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->state);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('HiveClusterStatus');
    if ($this->taskTrackers !== null) {
      $xfer += $output->writeFieldBegin('taskTrackers', TType::I32, 1);
      $xfer += $output->writeI32($this->taskTrackers);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->mapTasks !== null) {
      $xfer += $output->writeFieldBegin('mapTasks', TType::I32, 2);
      $xfer += $output->writeI32($this->mapTasks);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->reduceTasks !== null) {
      $xfer += $output->writeFieldBegin('reduceTasks', TType::I32, 3);
      $xfer += $output->writeI32($this->reduceTasks);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->maxMapTasks !== null) {
      $xfer += $output->writeFieldBegin('maxMapTasks', TType::I32, 4);
      $xfer += $output->writeI32($this->maxMapTasks);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->maxReduceTasks !== null) {
      $xfer += $output->writeFieldBegin('maxReduceTasks', TType::I32, 5);
      $xfer += $output->writeI32($this->maxReduceTasks);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->state !== null) {
      $xfer += $output->writeFieldBegin('state', TType::I32, 6);
      $xfer += $output->writeI32($this->state);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

class HiveServerException extends TException {
  static $_TSPEC;

  /**
   * @var string
   */
  public $message = null;
  /**
   * @var int
   */
  public $errorCode = null;
  /**
   * @var string
   */
  public $SQLState = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'message',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'errorCode',
          'type' => TType::I32,
          ),
        3 => array(
          'var' => 'SQLState',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['message'])) {
        $this->message = $vals['message'];
      }
      if (isset($vals['errorCode'])) {
        $this->errorCode = $vals['errorCode'];
      }
      if (isset($vals['SQLState'])) {
        $this->SQLState = $vals['SQLState'];
      }
    }
  }

  public function getName() {
    return 'HiveServerException';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->message);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::I32) {
            $xfer += $input->readI32($this->errorCode);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->SQLState);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('HiveServerException');
    if ($this->message !== null) {
      $xfer += $output->writeFieldBegin('message', TType::STRING, 1);
      $xfer += $output->writeString($this->message);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->errorCode !== null) {
      $xfer += $output->writeFieldBegin('errorCode', TType::I32, 2);
      $xfer += $output->writeI32($this->errorCode);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->SQLState !== null) {
      $xfer += $output->writeFieldBegin('SQLState', TType::STRING, 3);
      $xfer += $output->writeString($this->SQLState);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}


